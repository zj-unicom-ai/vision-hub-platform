package com.unicom.visionhub.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.visionhub.media.client.DeviceInternalClient;
import com.unicom.visionhub.web.cache.DeviceCache;
import com.unicom.visionhub.common.enums.AccessProtocol;
import com.unicom.visionhub.common.enums.DeviceStatus;
import com.unicom.visionhub.common.enums.DeviceType;
import com.unicom.visionhub.common.enums.StatusCode;
import com.unicom.visionhub.common.exception.ResourceExistsException;
import com.unicom.visionhub.common.exception.ResourceNotFoundException;
import com.unicom.visionhub.common.exception.ServiceException;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.web.dto.request.DeviceCreateRequest;
import com.unicom.visionhub.web.dto.request.DeviceQueryRequest;
import com.unicom.visionhub.web.dto.request.DeviceStatusCallbackRequest;
import com.unicom.visionhub.web.dto.request.DeviceUpdateRequest;
import com.unicom.visionhub.web.entity.device.Device;
import com.unicom.visionhub.web.mapper.DeviceMapper;
import com.unicom.visionhub.web.mapper.TaskDeviceMapper;
import com.unicom.visionhub.web.service.DeviceCheckService;
import com.unicom.visionhub.web.service.DeviceService;
import com.unicom.visionhub.web.service.FrameCaptureService;
import com.unicom.visionhub.web.service.MinioService;
import com.unicom.visionhub.web.vo.DeviceVO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    private final DeviceMapper deviceMapper;
    private final TaskDeviceMapper taskDeviceMapper;
    private final DeviceCheckService deviceCheckService;
    private final FrameCaptureService frameCaptureService;
    private final MinioService minioService;
    private final DeviceCache deviceCache;
    private final DeviceInternalClient deviceInternalClient;

    @Override
    public IPage<DeviceVO> page(DeviceQueryRequest queryRequest) {
        Page<Device> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<DeviceVO> resultPage = deviceMapper.selectPageWithVO(page, queryRequest);

        resultPage.getRecords().forEach(this::enrichVO);

        return resultPage;
    }

    @Override
    public DeviceVO getDetailById(Long id) {
        DeviceVO vo = deviceCache.getDeviceVO(id);
        if (vo == null) {
            log.warn("【设备】查询设备失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeviceCreateRequest createRequest) {
        if (deviceMapper.countByDeviceCode(createRequest.getDeviceCode(), null) > 0) {
            log.warn("【设备】创建设备失败，设备编码已存在: {}", createRequest.getDeviceCode());
            throw new ResourceExistsException("设备", "device_code", createRequest.getDeviceCode());
        }

        if (StrUtil.isBlank(createRequest.getAccessProtocol())
                || AccessProtocol.fromCode(createRequest.getAccessProtocol()) == null) {
            log.warn("【设备】创建设备失败，接入协议无效: {}", createRequest.getAccessProtocol());
            throw new ServiceException("接入协议无效，仅支持: stream(视频流地址), gb28181(GB28181协议)");
        }

        if (StrUtil.isBlank(createRequest.getDeviceType())
                || DeviceType.fromCode(createRequest.getDeviceType()) == null) {
            log.warn("【设备】创建设备失败，设备类型无效: {}", createRequest.getDeviceType());
            throw new ServiceException("设备类型无效，仅支持: ipc(网络摄像头), nvr(网络录像机)");
        }

        if (AccessProtocol.STREAM.getCode().equals(createRequest.getAccessProtocol())
                && StrUtil.isBlank(createRequest.getStreamUrl())) {
            log.warn("【设备】创建设备失败，视频流地址不能为空");
            throw new ServiceException("视频流地址不能为空");
        }

        Device entity = BeanUtil.copyProperties(createRequest, Device.class);
        entity.setSortOrder(createRequest.getSortOrder() != null ? createRequest.getSortOrder() : 0);
        entity.setDeviceStatus(DeviceStatus.UNKNOWN.getCode());

        boolean saved = save(entity);
        if (!saved) {
            log.error("【设备】创建设备失败，数据库操作失败");
            throw new SystemException("创建设备失败");
        }

        if (AccessProtocol.STREAM.getCode().equals(entity.getAccessProtocol())
                && StrUtil.isNotBlank(entity.getStreamUrl())) {
            asyncNotifyMediaStartStream(entity.getId(), entity.getStreamUrl());
        }

        log.info("【设备】创建设备成功, id={}, name={}", entity.getId(), entity.getDeviceName());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(DeviceUpdateRequest updateRequest) {
        Device existEntity = getById(updateRequest.getId());
        if (existEntity == null) {
            log.warn("【设备】更新设备失败，id={} 不存在", updateRequest.getId());
            throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
        }

        if (AccessProtocol.STREAM.getCode().equals(existEntity.getAccessProtocol())
                && StrUtil.isBlank(updateRequest.getStreamUrl())) {
            log.warn("【设备】更新设备失败，视频流地址不能为空");
            throw new ServiceException("视频流地址不能为空");
        }

        Device entity = BeanUtil.copyProperties(updateRequest, Device.class);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【设备】更新设备失败，数据库操作失败");
            throw new SystemException("更新设备失败");
        }

        if (AccessProtocol.STREAM.getCode().equals(existEntity.getAccessProtocol())
                && updateRequest.getStreamUrl() != null
                && !updateRequest.getStreamUrl().equals(existEntity.getStreamUrl())) {
            String deviceId = String.valueOf(updateRequest.getId());
            try {
                deviceInternalClient.stopStream(deviceId);
                deviceInternalClient.startStream(deviceId, updateRequest.getStreamUrl());
                log.info("【设备】流地址变更，通知 media 重启流: deviceId={}", updateRequest.getId());
            } catch (FeignException e) {
                log.warn("【设备】通知 media 重启流异常: deviceId={}, status={}", updateRequest.getId(), e.status());
            }
        }

        deviceCache.evictDeviceVO(updateRequest.getId());

        log.info("【设备】更新设备成功, id={}", updateRequest.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Device existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【设备】删除设备失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
        }

        int taskCount = taskDeviceMapper.countByDeviceId(id);
        if (taskCount > 0) {
            log.warn("【设备】删除设备失败，被 {} 个任务点位引用", taskCount);
            throw new ServiceException(StatusCode.DEVICE_IN_USE_BY_TASK.getMessage());
        }

        Device device = getById(id);
        if (device != null && AccessProtocol.STREAM.getCode().equals(device.getAccessProtocol())) {
            try {
                deviceInternalClient.stopStream(String.valueOf(id));
                log.info("【设备】删除设备前通知 media 停止拉流: deviceId={}", id);
            } catch (FeignException e) {
                log.warn("【设备】通知 media 停止拉流异常: deviceId={}, status={}", id, e.status());
            }
        }

        boolean removed = removeById(id);
        if (!removed) {
            log.error("【设备】删除设备失败，数据库操作失败");
            throw new SystemException("删除设备失败");
        }

        deviceCache.evictDeviceVO(id);

        log.info("【设备】删除设备成功, id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        Device existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【设备】更新设备状态失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
        }

        Device entity = new Device();
        entity.setId(id);
        entity.setStatus(status);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【设备】更新设备状态失败，数据库操作失败");
            throw new SystemException("更新状态失败");
        }

        String deviceId = String.valueOf(id);
        if (status != null && status == 0) {
            try {
                deviceInternalClient.stopStream(deviceId);
                log.info("【设备】设备禁用，通知 media 停止拉流: deviceId={}", id);
            } catch (FeignException e) {
                log.warn("【设备】通知 media 停止拉流异常: deviceId={}, status={}", id, e.status());
            }
        } else if (status != null && status == 1) {
            Device device = getById(id);
            if (device != null && AccessProtocol.STREAM.getCode().equals(device.getAccessProtocol())
                    && StrUtil.isNotBlank(device.getStreamUrl())) {
                try {
                    deviceInternalClient.startStream(deviceId, device.getStreamUrl());
                    log.info("【设备】设备启用，通知 media 开始拉流: deviceId={}", id);
                } catch (FeignException e) {
                    log.warn("【设备】通知 media 开始拉流异常: deviceId={}, status={}", id, e.status());
                }
            }
        }

        log.info("【设备】更新设备状态成功, id={}, status={}", id, status);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkDeviceStatus(Long id) {
        Device device = getById(id);
        if (device == null) {
            log.warn("【设备】手动检测设备状态失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
        }

        boolean isOnline = deviceCheckService.checkDevice(device);
        log.info("【设备】手动检测设备状态完成, id={}, name={}, status={}",
                id, device.getDeviceName(), isOnline ? "online" : "offline");
        return isOnline;
    }

    @Override
    public String captureFrame(Long id) {
        Device device = getById(id);
        if (device == null || device.getDeleted() == 1) {
            log.warn("【设备】截帧失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
        }

        if (device.getStatus() != null && device.getStatus() == 0) {
            log.warn("【设备】截帧失败，设备已禁用: id={}", id);
            throw new ServiceException("设备已禁用，无法截帧");
        }

        if (StrUtil.isBlank(device.getStreamUrl())) {
            log.warn("【设备】截帧失败，视频流地址为空: id={}", id);
            throw new ServiceException("设备未配置视频流地址");
        }

        String snapshotObjectName = frameCaptureService.captureByDevice(id, device.getStreamUrl());
        if (StrUtil.isBlank(snapshotObjectName)) {
            log.warn("【设备】截帧失败，无法从视频流获取画面: id={}", id);
            throw new ServiceException("截帧失败，请检查视频流是否正常");
        }

        String snapshotUrl = minioService.getFileUrlWithCdn(snapshotObjectName);
        log.info("【设备】截帧成功, id={}, url={}", id, snapshotUrl);
        return snapshotUrl;
    }

    @Override
    public String play(Long id) {
        DeviceVO vo = deviceCache.getDeviceVO(id);
        if (vo == null) {
            log.warn("【设备】播放失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
        }

        if (vo.getStatus() != null && vo.getStatus() == 0) {
            log.warn("【设备】播放失败，设备已禁用: id={}", id);
            throw new ServiceException("设备已禁用，无法播放");
        }

        if (!DeviceStatus.ONLINE.getCode().equals(vo.getDeviceStatus())) {
            log.warn("【设备】播放失败，设备状态异常: id={}, status={}", id, vo.getDeviceStatus());
            throw new ServiceException("设备状态异常，无法播放");
        }

        if (AccessProtocol.STREAM.getCode().equals(vo.getAccessProtocol())) {
            log.info("【设备】获取播放地址成功, id={}, protocol=stream", id);
            return vo.getStreamUrl();
        }

        log.info("【设备】获取播放地址, id={}, protocol=gb28181, 暂不支持", id);
        return null;
    }

    @Override
    public void statusCallback(Long id, DeviceStatusCallbackRequest request) {
        Device device = getById(id);
        if (device == null || device.getDeleted() == 1) {
            log.warn("【设备】状态回调失败，设备不存在或已删除: id={}", id);
            return;
        }

        String newStatus = "online".equals(request.getDeviceStatus())
                ? DeviceStatus.ONLINE.getCode()
                : DeviceStatus.OFFLINE.getCode();
        String currentStatus = device.getDeviceStatus();

        if (Objects.equals(newStatus, currentStatus)) {
            log.debug("【设备】状态回调状态未变化({})，跳过更新: id={}", currentStatus, id);
            return;
        }

        Device updateEntity = new Device();
        updateEntity.setId(id);
        updateEntity.setDeviceStatus(newStatus);
        updateEntity.setLastCheckTime(LocalDateTime.now());
        updateEntity.setLastCheckStatus(request.getCheckStatus());
        updateById(updateEntity);

        deviceCache.evictDeviceVO(id);

        log.info("【设备】收到 media 状态回调更新: id={}, {} -> {}", id, currentStatus, newStatus);
    }


    @Async
    public void asyncNotifyMediaStartStream(Long deviceId, String streamUrl) {
        try {
            boolean started = deviceInternalClient.startStream(String.valueOf(deviceId), streamUrl);
            log.info("【设备】异步通知 media 拉流: deviceId={}, started={}", deviceId, started);
        } catch (FeignException e) {
            log.warn("【设备】通知 media 拉流异常: deviceId={}, status={}", deviceId, e.status());
        }
    }


    private void enrichVO(DeviceVO vo) {
        DeviceType deviceType = DeviceType.fromCode(vo.getDeviceType());
        vo.setDeviceTypeDesc(deviceType != null ? deviceType.getDescription() : "未知");

        AccessProtocol accessProtocol = AccessProtocol.fromCode(vo.getAccessProtocol());
        vo.setAccessProtocolDesc(accessProtocol != null ? accessProtocol.getDescription() : "未知");

        DeviceStatus deviceStatus = DeviceStatus.fromCode(vo.getDeviceStatus());
        vo.setDeviceStatusDesc(deviceStatus != null ? deviceStatus.getDescription() : "未知");

        vo.setStatusDesc(vo.getStatus() == 1 ? "启用" : "禁用");
    }
}
