package com.unicom.visionhub.web.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.visionhub.common.enums.EventRecordStatus;
import com.unicom.visionhub.common.enums.EventType;
import com.unicom.visionhub.common.enums.StatusCode;
import com.unicom.visionhub.common.exception.ResourceNotFoundException;
import com.unicom.visionhub.common.exception.ServiceException;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.web.dto.request.EventRecordQueryRequest;
import com.unicom.visionhub.web.entity.event.EventRecord;
import com.unicom.visionhub.web.excel.EventRecordExcelVO;
import com.unicom.visionhub.web.excel.EventRecordExcelStyleHandler;
import com.unicom.visionhub.web.mapper.EventRecordMapper;
import com.unicom.visionhub.web.service.EventRecordService;
import com.unicom.visionhub.web.service.MinioService;
import com.unicom.visionhub.web.vo.EventRecordVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventRecordServiceImpl extends ServiceImpl<EventRecordMapper, EventRecord> implements EventRecordService {

    private final EventRecordMapper eventRecordMapper;
    private final MinioService minioService;

    @Override
    public IPage<EventRecordVO> page(EventRecordQueryRequest queryRequest) {
        Page<EventRecord> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<EventRecordVO> resultPage = eventRecordMapper.selectPageWithVO(page, queryRequest);
        resultPage.getRecords().forEach(this::enrichVO);
        return resultPage;
    }

    @Override
    public EventRecordVO getDetailById(Long id) {
        EventRecordVO vo = eventRecordMapper.selectVOById(id);
        if (vo == null) {
            log.warn("【事件】查询事件记录失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.EVENT_NOT_FOUND);
        }
        enrichVO(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, String status) {
        EventRecord existRecord = getById(id);
        if (existRecord == null) {
            log.warn("【事件】更新事件状态失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.EVENT_NOT_FOUND);
        }

        EventRecordStatus recordStatus = EventRecordStatus.fromCode(status);
        if (recordStatus == null) {
            log.warn("【事件】更新事件状态失败，非法状态: {}", status);
            throw new ServiceException(StatusCode.EVENT_STATUS_INVALID);
        }

        EventRecord record = new EventRecord();
        record.setId(id);
        record.setStatus(status);

        boolean updated = updateById(record);
        if (!updated) {
            log.error("【事件】更新事件状态失败，数据库操作失败");
            throw new SystemException("更新事件状态失败");
        }

        log.info("【事件】更新事件状态成功, id={}, status={}", id, status);
        return true;
    }

    @Override
    public void export(EventRecordQueryRequest queryRequest, HttpServletResponse response) {
        log.info("【事件】导出事件记录: {}", queryRequest);

        List<EventRecordVO> records = eventRecordMapper.selectListWithVO(queryRequest);
        records.forEach(this::enrichVO);

        List<EventRecordExcelVO> excelList = records.stream()
                .map(this::convertToExcelVO)
                .collect(Collectors.toList());

        String fileName = URLEncoder.encode(
                "事件记录_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx",
                StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        try {
            EasyExcel.write(response.getOutputStream(), EventRecordExcelVO.class)
                    .registerWriteHandler(new EventRecordExcelStyleHandler())
                    .sheet("事件记录")
                    .doWrite(excelList);
        } catch (IOException e) {
            log.error("【事件】导出事件记录失败，IO异常", e);
            throw new SystemException("导出事件记录失败");
        }

        log.info("【事件】导出事件记录成功，共 {} 条", excelList.size());
    }


    private EventRecordExcelVO convertToExcelVO(EventRecordVO vo) {
        EventRecordExcelVO excelVO = new EventRecordExcelVO();
        excelVO.setEventName(vo.getEventName());
        excelVO.setDeviceName(vo.getDeviceName());
        excelVO.setTaskName(vo.getTaskName());
        excelVO.setAlgorithmName(vo.getAlgorithmName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (vo.getCreatedAt() != null) {
            excelVO.setCreatedAt(vo.getCreatedAt().format(formatter));
        }

        return excelVO;
    }


    private void enrichVO(EventRecordVO vo) {
        EventRecordStatus status = EventRecordStatus.fromCode(vo.getStatus());
        vo.setStatusDesc(status != null ? status.getDescription() : "未知");

        EventType eventType = EventType.fromCode(vo.getEventType());
        vo.setEventTypeDesc(eventType != null ? eventType.getDescription() : "未知");

        if (vo.getAlarmImageUrl() != null && !vo.getAlarmImageUrl().isEmpty()) {
            vo.setAlarmImageUrl(minioService.getFileUrlWithCdn(vo.getAlarmImageUrl()));
        }

        vo.setEventDescription(parseEventDescription(vo.getAnalysisResult()));
    }


    @SuppressWarnings("unchecked")
    private String parseEventDescription(String analysisResult) {
        if (analysisResult == null || analysisResult.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> outputData = JSONUtil.toBean(analysisResult, Map.class);
            Map<String, Object> resultMap = outputData != null ? (Map<String, Object>) outputData.get("result") : null;
            Map<String, Object> dataMap = resultMap != null ? (Map<String, Object>) resultMap.get("data") : null;
            if (dataMap != null && dataMap.get("description") != null) {
                return dataMap.get("description").toString();
            }
        } catch (Exception e) {
            log.debug("【事件】解析事件说明失败: {}", analysisResult);
        }
        return null;
    }
}
