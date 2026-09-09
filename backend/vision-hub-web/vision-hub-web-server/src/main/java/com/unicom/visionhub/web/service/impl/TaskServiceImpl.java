package com.unicom.visionhub.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.visionhub.common.enums.AlgorithmStatus;
import com.unicom.visionhub.common.enums.EventType;
import com.unicom.visionhub.common.enums.StatusCode;
import com.unicom.visionhub.common.enums.TaskStatus;
import com.unicom.visionhub.common.exception.ResourceExistsException;
import com.unicom.visionhub.common.exception.ResourceNotFoundException;
import com.unicom.visionhub.common.exception.ServiceException;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.web.cache.EventCache;
import com.unicom.visionhub.web.cache.TaskAlgorithmCache;
import com.unicom.visionhub.web.cache.TaskCache;
import com.unicom.visionhub.web.dto.EffectivePeriod;
import com.unicom.visionhub.web.dto.ScheduleRule;
import com.unicom.visionhub.web.dto.request.TaskAlgorithmConfigRequest;
import com.unicom.visionhub.web.dto.request.TaskCreateRequest;
import com.unicom.visionhub.web.dto.request.TaskDeviceAddRequest;
import com.unicom.visionhub.web.dto.request.TaskDeviceConfigRequest;
import com.unicom.visionhub.web.dto.request.TaskDeviceQueryRequest;
import com.unicom.visionhub.web.dto.request.TaskQueryRequest;
import com.unicom.visionhub.web.dto.request.TaskUpdateRequest;
import com.unicom.visionhub.web.entity.algorithm.Algorithm;
import com.unicom.visionhub.web.entity.device.Device;
import com.unicom.visionhub.web.entity.event.Event;
import com.unicom.visionhub.web.entity.task.Task;
import com.unicom.visionhub.web.entity.task.TaskAlgorithm;
import com.unicom.visionhub.web.entity.task.TaskDevice;
import com.unicom.visionhub.web.mapper.AlgorithmMapper;
import com.unicom.visionhub.web.mapper.DeviceMapper;
import com.unicom.visionhub.web.mapper.EventMapper;
import com.unicom.visionhub.web.mapper.TaskAlgorithmMapper;
import com.unicom.visionhub.web.mapper.TaskDeviceMapper;
import com.unicom.visionhub.web.mapper.TaskMapper;
import com.unicom.visionhub.web.service.TaskService;
import com.unicom.visionhub.web.task.TaskAnalysisEngine;
import com.unicom.visionhub.web.vo.TaskAlgorithmDetailVO;
import com.unicom.visionhub.web.vo.TaskAlgorithmVO;
import com.unicom.visionhub.web.vo.TaskDetailVO;
import com.unicom.visionhub.web.vo.TaskDeviceDetailVO;
import com.unicom.visionhub.web.vo.TaskDeviceVO;
import com.unicom.visionhub.web.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskAlgorithmMapper taskAlgorithmMapper;
    private final TaskDeviceMapper taskDeviceMapper;
    private final AlgorithmMapper algorithmMapper;
    private final DeviceMapper deviceMapper;
    private final EventMapper eventMapper;
    private final EventCache eventCache;
    private final TaskCache taskCache;
    private final TaskAlgorithmCache taskAlgorithmCache;
    private final TaskAnalysisEngine taskAnalysisEngine;

    @Override
    public IPage<TaskVO> page(TaskQueryRequest queryRequest) {
        Page<Task> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<TaskVO> resultPage = taskMapper.selectPageWithVO(page, queryRequest);

        if (!CollectionUtils.isEmpty(resultPage.getRecords())) {
            List<Long> taskIds = resultPage.getRecords().stream()
                    .map(TaskVO::getId)
                    .collect(Collectors.toList());

            List<TaskAlgorithm> taskAlgorithms = taskAlgorithmMapper.selectList(
                    new LambdaQueryWrapper<TaskAlgorithm>()
                            .in(TaskAlgorithm::getTaskId, taskIds));

            if (!CollectionUtils.isEmpty(taskAlgorithms)) {
                List<Long> algorithmIds = taskAlgorithms.stream()
                        .map(TaskAlgorithm::getAlgorithmId)
                        .distinct()
                        .collect(Collectors.toList());

                List<Algorithm> algorithms = algorithmMapper.selectBatchIds(algorithmIds);
                Map<Long, String> algorithmNameMap = algorithms.stream()
                        .filter(a -> a.getDeleted() == null || a.getDeleted() == 0)
                        .collect(Collectors.toMap(Algorithm::getId, Algorithm::getAlgorithmName));

                Map<Long, List<String>> taskAlgorithmNameMap = new HashMap<>();
                for (TaskAlgorithm ta : taskAlgorithms) {
                    String name = algorithmNameMap.get(ta.getAlgorithmId());
                    if (name != null) {
                        taskAlgorithmNameMap
                                .computeIfAbsent(ta.getTaskId(), k -> new ArrayList<>())
                                .add(name);
                    }
                }

                resultPage.getRecords().forEach(vo ->
                        vo.setAlgorithmNames(taskAlgorithmNameMap.getOrDefault(vo.getId(), new ArrayList<>())));
            }
        }

        resultPage.getRecords().forEach(this::enrichVO);
        return resultPage;
    }

    @Override
    public TaskDetailVO getDetailById(Long id) {
        TaskVO vo = taskCache.getTaskVO(id);
        if (vo == null) {
            log.warn("【任务】查询任务失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }
        enrichVO(vo);

        TaskDetailVO detailVO = new TaskDetailVO();
        BeanUtil.copyProperties(vo, detailVO);

        List<TaskAlgorithmVO> algorithmVOs = taskAlgorithmMapper.selectListByTaskId(id);
        List<TaskAlgorithmDetailVO> algorithms = new ArrayList<>();

        for (TaskAlgorithmVO algorithmVO : algorithmVOs) {
            TaskAlgorithmDetailVO detailAlgorithm = new TaskAlgorithmDetailVO();
            BeanUtil.copyProperties(algorithmVO, detailAlgorithm);

            Long algorithmId = algorithmVO.getAlgorithmId();

            List<TaskDevice> devices = taskDeviceMapper.selectList(
                    new LambdaQueryWrapper<TaskDevice>()
                            .eq(TaskDevice::getTaskId, id)
                            .eq(TaskDevice::getAlgorithmId, algorithmId));

            List<TaskDeviceDetailVO> deviceDetailVOs = new ArrayList<>();
            for (TaskDevice device : devices) {
                TaskDeviceDetailVO deviceDetailVO = new TaskDeviceDetailVO();
                deviceDetailVO.setDeviceId(device.getDeviceId());

                deviceDetailVOs.add(deviceDetailVO);
            }

            detailAlgorithm.setDevices(deviceDetailVOs);
            detailAlgorithm.setDeviceCount(devices.size());
            algorithms.add(detailAlgorithm);
        }

        detailVO.setAlgorithms(algorithms);
        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TaskCreateRequest createRequest) {
        if (taskMapper.countByTaskName(createRequest.getTaskName(), null) > 0) {
            log.warn("【任务】创建任务失败，任务名称已存在: {}", createRequest.getTaskName());
            throw new ResourceExistsException("任务", "task_name", createRequest.getTaskName());
        }

        validateAlgorithms(createRequest.getAlgorithmConfigs());

        validateAllAlgorithmDevices(createRequest.getAlgorithmConfigs());

        validateAlgorithmConfigs(createRequest.getAlgorithmConfigs());

        if (CollectionUtils.isEmpty(createRequest.getAlgorithmConfigs())) {
            log.warn("【任务】创建任务失败，任务必须绑定至少一个算法");
            throw new ServiceException(StatusCode.TASK_ALGORITHM_REQUIRED);
        }

        Task task = new Task();
        task.setTaskName(createRequest.getTaskName());
        task.setTaskStatus(TaskStatus.DRAFT.getCode());
        task.setStatus(1);
        task.setRemark(createRequest.getRemark());
        task.setSortOrder(0);

        boolean saved = save(task);
        if (!saved) {
            log.error("【任务】创建任务失败，数据库操作失败");
            throw new SystemException("创建任务失败");
        }

        createTaskAlgorithms(task.getId(), createRequest.getAlgorithmConfigs());

        log.info("【任务】创建任务成功, id={}, name={}", task.getId(), task.getTaskName());
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TaskUpdateRequest updateRequest) {
        Task existTask = getById(updateRequest.getId());
        if (existTask == null) {
            log.warn("【任务】更新任务失败，id={} 不存在", updateRequest.getId());
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        if (updateRequest.getTaskName() != null
                && !updateRequest.getTaskName().equals(existTask.getTaskName())
                && taskMapper.countByTaskName(updateRequest.getTaskName(), updateRequest.getId()) > 0) {
            log.warn("【任务】更新任务失败，任务名称已存在: {}", updateRequest.getTaskName());
            throw new ResourceExistsException("任务", "task_name", updateRequest.getTaskName());
        }

        Task task = new Task();
        task.setId(updateRequest.getId());
        task.setTaskName(updateRequest.getTaskName());
        task.setRemark(updateRequest.getRemark());

        boolean updated = updateById(task);
        if (!updated) {
            log.error("【任务】更新任务失败，数据库操作失败");
            throw new SystemException("更新任务失败");
        }

        if (updateRequest.getAlgorithmConfigs() != null) {
            if (CollectionUtils.isEmpty(updateRequest.getAlgorithmConfigs())) {
                log.warn("【任务】更新任务失败，任务必须绑定至少一个算法");
                throw new ServiceException(StatusCode.TASK_ALGORITHM_REQUIRED);
            }
            validateAlgorithms(updateRequest.getAlgorithmConfigs());
            validateAllAlgorithmDevices(updateRequest.getAlgorithmConfigs());
            validateAlgorithmConfigs(updateRequest.getAlgorithmConfigs());

            updateTaskAlgorithms(updateRequest.getId(), updateRequest.getAlgorithmConfigs());

            taskAlgorithmCache.evictByTaskId(updateRequest.getId());
        }

        log.info("【任务】更新任务成功, id={}", updateRequest.getId());

        taskCache.evictTask(updateRequest.getId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Task existTask = getById(id);
        if (existTask == null) {
            log.warn("【任务】删除任务失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        if (TaskStatus.RUNNING.getCode().equals(existTask.getTaskStatus())) {
            taskAnalysisEngine.stopTask(id);
        }

        boolean removed = removeById(id);
        if (!removed) {
            log.error("【任务】删除任务失败，数据库操作失败");
            throw new SystemException("删除任务失败");
        }

        taskAlgorithmMapper.delete(
                new LambdaQueryWrapper<TaskAlgorithm>()
                        .eq(TaskAlgorithm::getTaskId, id));
        taskDeviceMapper.deleteByTaskId(id);

        eventMapper.deleteByTaskId(id);
        eventCache.removeTask(id);

        taskCache.evictTask(id);
        taskAlgorithmCache.evictByTaskId(id);

        log.info("【任务】删除任务成功, id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean start(Long id) {
        Task existTask = getById(id);
        if (existTask == null) {
            log.warn("【任务】启动任务失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        String currentStatus = existTask.getTaskStatus();
        if (!TaskStatus.DRAFT.getCode().equals(currentStatus)) {
            log.warn("【任务】启动任务失败，当前状态不允许启动: id={}, status={}", id, currentStatus);
            throw new ServiceException("当前任务状态不允许启动");
        }

        validateTaskAlgorithmsStatus(id);

        Task task = new Task();
        task.setId(id);
        task.setTaskStatus(TaskStatus.RUNNING.getCode());

        boolean updated = updateById(task);
        if (!updated) {
            log.error("【任务】启动任务失败，数据库操作失败");
            throw new SystemException("启动任务失败");
        }

        runAfterCommit(() -> {
            taskCache.evictTask(id);
            taskAnalysisEngine.startTask(id);
        });

        log.info("【任务】启动任务成功, id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stop(Long id) {
        Task existTask = getById(id);
        if (existTask == null) {
            log.warn("【任务】停止任务失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        String currentStatus = existTask.getTaskStatus();
        if (!TaskStatus.RUNNING.getCode().equals(currentStatus)) {
            log.warn("【任务】停止任务失败，当前状态不允许停止: id={}, status={}", id, currentStatus);
            throw new ServiceException("当前任务状态不允许停止");
        }

        Task task = new Task();
        task.setId(id);
        task.setTaskStatus(TaskStatus.DRAFT.getCode());

        boolean updated = updateById(task);
        if (!updated) {
            log.error("【任务】停止任务失败，数据库操作失败");
            throw new SystemException("停止任务失败");
        }

        runAfterCommit(() -> {
            taskCache.evictTask(id);
            taskAnalysisEngine.stopTask(id);
        });

        log.info("【任务】停止任务成功, id={}", id);
        return true;
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    @Override
    public IPage<TaskDeviceVO> getTaskDevices(Long taskId, Long algorithmId, TaskDeviceQueryRequest queryRequest) {
        Task existTask = getById(taskId);
        if (existTask == null) {
            log.warn("【任务】查询任务点位失败，id={} 不存在", taskId);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        validateAlgorithmInTask(taskId, algorithmId);

        Page<TaskDevice> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<TaskDeviceVO> resultPage = taskDeviceMapper.selectPageByTaskIdAndAlgorithmId(page, taskId, algorithmId, queryRequest);
        resultPage.getRecords().forEach(this::enrichDeviceVO);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addTaskDevices(Long taskId, Long algorithmId, TaskDeviceAddRequest addRequest) {
        Task existTask = getById(taskId);
        if (existTask == null) {
            log.warn("【任务】添加点位失败，任务id={} 不存在", taskId);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        validateAlgorithmInTask(taskId, algorithmId);

        List<Long> deviceIds = addRequest.getDeviceIds();
        if (CollectionUtils.isEmpty(deviceIds)) {
            return true;
        }

        validateDevices(deviceIds);

        List<TaskDevice> existDevices = taskDeviceMapper.selectList(
                new LambdaQueryWrapper<TaskDevice>()
                        .eq(TaskDevice::getTaskId, taskId)
                        .eq(TaskDevice::getAlgorithmId, algorithmId));
        List<Long> existDeviceIds = existDevices.stream()
                .map(TaskDevice::getDeviceId)
                .collect(Collectors.toList());

        List<Long> newDeviceIds = deviceIds.stream()
                .filter(deviceId -> !existDeviceIds.contains(deviceId))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(newDeviceIds)) {
            log.info("【任务】添加点位完成，无新点位需要添加, taskId={}, algorithmId={}", taskId, algorithmId);
            return true;
        }

        Boolean enabled = addRequest.getEnabled();
        Integer enabledValue = Boolean.TRUE.equals(enabled) ? 1 : 0;
        List<TaskDevice> taskDevices = new ArrayList<>();
        for (Long deviceId : newDeviceIds) {
            TaskDevice taskDevice = new TaskDevice();
            taskDevice.setTaskId(taskId);
            taskDevice.setAlgorithmId(algorithmId);
            taskDevice.setDeviceId(deviceId);
            taskDevice.setEnabled(enabledValue);
            taskDevices.add(taskDevice);
        }
        taskDeviceMapper.insertBatch(taskDevices);

        log.info("【任务】添加点位成功, taskId={}, algorithmId={}, count={}, enabled={}", taskId, algorithmId, newDeviceIds.size(), enabledValue);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeTaskDevice(Long taskId, Long algorithmId, Long deviceId) {
        Task existTask = getById(taskId);
        if (existTask == null) {
            log.warn("【任务】移除点位失败，任务id={} 不存在", taskId);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        int deleted = taskDeviceMapper.deleteByTaskIdAlgorithmIdAndDeviceId(taskId, algorithmId, deviceId);
        if (deleted == 0) {
            log.warn("【任务】移除点位失败，点位不存在, taskId={}, algorithmId={}, deviceId={}", taskId, algorithmId, deviceId);
            throw new ResourceNotFoundException(StatusCode.TASK_DEVICE_NOT_FOUND);
        }

        log.info("【任务】移除点位成功, taskId={}, algorithmId={}, deviceId={}", taskId, algorithmId, deviceId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDeviceEnabled(Long taskId, Long algorithmId, Long deviceId, Boolean enabled) {
        Task existTask = getById(taskId);
        if (existTask == null) {
            log.warn("【任务】更新点位启用状态失败，任务id={} 不存在", taskId);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        TaskDevice taskDevice = taskDeviceMapper.selectOne(
                new LambdaQueryWrapper<TaskDevice>()
                        .eq(TaskDevice::getTaskId, taskId)
                        .eq(TaskDevice::getAlgorithmId, algorithmId)
                        .eq(TaskDevice::getDeviceId, deviceId));
        if (taskDevice == null) {
            log.warn("【任务】更新点位启用状态失败，点位不存在, taskId={}, algorithmId={}, deviceId={}", taskId, algorithmId, deviceId);
            throw new ResourceNotFoundException(StatusCode.TASK_DEVICE_NOT_FOUND);
        }

        Integer enabledValue = Boolean.TRUE.equals(enabled) ? 1 : 0;
        taskDevice.setEnabled(enabledValue);
        taskDeviceMapper.updateById(taskDevice);

        log.info("【任务】更新点位启用状态成功, taskId={}, algorithmId={}, deviceId={}, enabled={}", taskId, algorithmId, deviceId, enabledValue);
        return true;
    }

    @Override
    public IPage<TaskAlgorithmVO> queryTaskAlgorithms(String algorithmName, Integer pageNum, Integer pageSize) {
        Page<TaskAlgorithm> page = new Page<>(pageNum, pageSize);
        return taskAlgorithmMapper.selectPageByAlgorithmName(page, algorithmName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long id) {
        Task sourceTask = getById(id);
        if (sourceTask == null) {
            log.warn("【任务】复制任务失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.TASK_NOT_FOUND);
        }

        String newTaskName = generateCopyTaskName(sourceTask.getTaskName());

        Task newTask = new Task();
        newTask.setTaskName(newTaskName);
        newTask.setTaskStatus(TaskStatus.DRAFT.getCode());
        newTask.setStatus(sourceTask.getStatus());
        newTask.setRemark(sourceTask.getRemark());
        newTask.setSortOrder(0);

        boolean saved = save(newTask);
        if (!saved) {
            log.error("【任务】复制任务失败，数据库操作失败");
            throw new SystemException("复制任务失败");
        }
        Long newTaskId = newTask.getId();

        List<TaskAlgorithm> sourceAlgorithms = taskAlgorithmMapper.selectList(
                new LambdaQueryWrapper<TaskAlgorithm>()
                        .eq(TaskAlgorithm::getTaskId, id));
        if (!CollectionUtils.isEmpty(sourceAlgorithms)) {
            List<TaskAlgorithm> newAlgorithms = new ArrayList<>();
            for (TaskAlgorithm source : sourceAlgorithms) {
                Event sourceEvent = eventMapper.selectById(source.getEventId());
                Long newEventId = null;
                if (sourceEvent != null) {
                    Event newEvent = new Event();
                    newEvent.setTaskId(newTaskId);
                    newEvent.setAlgorithmId(sourceEvent.getAlgorithmId());
                    newEvent.setEventName(sourceEvent.getEventName());
                    newEvent.setEventType(sourceEvent.getEventType());
                    newEvent.setStatus(sourceEvent.getStatus());
                    eventMapper.insert(newEvent);
                    newEventId = newEvent.getId();
                }

                TaskAlgorithm algorithm = new TaskAlgorithm();
                algorithm.setTaskId(newTaskId);
                algorithm.setAlgorithmId(source.getAlgorithmId());
                algorithm.setSampleInterval(source.getSampleInterval());
                algorithm.setValidityEnd(source.getValidityEnd());
                algorithm.setEventId(newEventId);
                algorithm.setScheduleRules(source.getScheduleRules());
                newAlgorithms.add(algorithm);
            }
            taskAlgorithmMapper.insertBatch(newAlgorithms);
        }

        eventCache.refreshTask(newTaskId);

        List<TaskDevice> sourceDevices = taskDeviceMapper.selectList(
                new LambdaQueryWrapper<TaskDevice>()
                        .eq(TaskDevice::getTaskId, id));
        if (!CollectionUtils.isEmpty(sourceDevices)) {
            List<TaskDevice> newDevices = new ArrayList<>();
            for (TaskDevice source : sourceDevices) {
                TaskDevice device = new TaskDevice();
                device.setTaskId(newTaskId);
                device.setAlgorithmId(source.getAlgorithmId());
                device.setDeviceId(source.getDeviceId());
                device.setEnabled(source.getEnabled());
                newDevices.add(device);
            }
            taskDeviceMapper.insertBatch(newDevices);
        }

        log.info("【任务】复制任务成功, sourceId={}, newId={}, newName={}", id, newTaskId, newTaskName);
        return newTaskId;
    }


    private String generateCopyTaskName(String originalName) {
        int index = 1;
        while (true) {
            String candidate = originalName + "（" + index + "）";
            if (taskMapper.countByTaskName(candidate, null) == 0) {
                return candidate;
            }
            index++;
            if (index > 9999) {
                throw new SystemException("生成任务名称失败，序号超限");
            }
        }
    }


    private void validateTaskAlgorithmsStatus(Long taskId) {
        List<TaskAlgorithm> taskAlgorithms = taskAlgorithmMapper.selectList(
                new LambdaQueryWrapper<TaskAlgorithm>()
                        .eq(TaskAlgorithm::getTaskId, taskId));
        if (CollectionUtils.isEmpty(taskAlgorithms)) {
            return;
        }

        List<Long> algorithmIds = taskAlgorithms.stream()
                .map(TaskAlgorithm::getAlgorithmId)
                .distinct()
                .collect(Collectors.toList());

        List<Algorithm> algorithms = algorithmMapper.selectBatchIds(algorithmIds);
        Map<Long, Algorithm> algorithmMap = algorithms.stream()
                .collect(Collectors.toMap(Algorithm::getId, a -> a));

        for (Long algorithmId : algorithmIds) {
            Algorithm algorithm = algorithmMap.get(algorithmId);
            if (algorithm == null || algorithm.getDeleted() == 1) {
                log.warn("【任务】启动任务失败，关联算法已删除, taskId={}, algorithmId={}", taskId, algorithmId);
                throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
            }
            if (algorithm.getStatus() == null || !algorithm.getStatus().equals(AlgorithmStatus.PUBLISHED.getValue())) {
                log.warn("【任务】启动任务失败，关联算法已下架, taskId={}, algorithmId={}, algorithmCode={}",
                        taskId, algorithmId, algorithm.getAlgorithmCode());
                throw new ServiceException(StatusCode.ALGORITHM_DISABLED);
            }
        }
    }


    private void validateAlgorithms(List<TaskAlgorithmConfigRequest> algorithmConfigs) {
        if (CollectionUtils.isEmpty(algorithmConfigs)) {
            return;
        }
        List<Long> algorithmIds = algorithmConfigs.stream()
                .map(TaskAlgorithmConfigRequest::getAlgorithmId)
                .distinct()
                .collect(Collectors.toList());
        List<Algorithm> algorithms = algorithmMapper.selectBatchIds(algorithmIds);
        if (algorithms.size() != algorithmIds.size()) {
            List<Long> foundIds = algorithms.stream().map(Algorithm::getId).collect(Collectors.toList());
            List<Long> missingIds = algorithmIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            log.warn("【任务】算法不存在, algorithmIds={}", missingIds);
            throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
        }
        for (Algorithm algorithm : algorithms) {
            if (algorithm.getDeleted() == 1) {
                log.warn("【任务】算法已删除, algorithmId={}", algorithm.getId());
                throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
            }
            if (algorithm.getStatus() == null || !algorithm.getStatus().equals(AlgorithmStatus.PUBLISHED.getValue())) {
                log.warn("【任务】算法未上架, algorithmId={}, algorithmCode={}", algorithm.getId(), algorithm.getAlgorithmCode());
                throw new ServiceException(StatusCode.ALGORITHM_DISABLED);
            }
        }
    }


    private void validateAlgorithmInTask(Long taskId, Long algorithmId) {
        Long count = taskAlgorithmMapper.selectCount(
                new LambdaQueryWrapper<TaskAlgorithm>()
                        .eq(TaskAlgorithm::getTaskId, taskId)
                        .eq(TaskAlgorithm::getAlgorithmId, algorithmId));
        if (count == null || count == 0) {
            log.warn("【任务】算法不在任务中, taskId={}, algorithmId={}", taskId, algorithmId);
            throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
        }
    }


    private void validateAllAlgorithmDevices(List<TaskAlgorithmConfigRequest> algorithmConfigs) {
        if (CollectionUtils.isEmpty(algorithmConfigs)) {
            return;
        }
        Set<Long> allDeviceIds = new HashSet<>();
        for (TaskAlgorithmConfigRequest config : algorithmConfigs) {
            if (!CollectionUtils.isEmpty(config.getDevices())) {
                for (TaskDeviceConfigRequest device : config.getDevices()) {
                    allDeviceIds.add(device.getDeviceId());
                }
            }
        }
        validateDevices(new ArrayList<>(allDeviceIds));
    }


    private void validateDevices(List<Long> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return;
        }
        List<Long> distinctDeviceIds = deviceIds.stream().distinct().collect(Collectors.toList());
        List<Device> devices = deviceMapper.selectBatchIds(distinctDeviceIds);
        if (devices.size() != distinctDeviceIds.size()) {
            List<Long> foundIds = devices.stream().map(Device::getId).collect(Collectors.toList());
            List<Long> missingIds = distinctDeviceIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            log.warn("【任务】设备不存在, deviceIds={}", missingIds);
            throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
        }
        for (Device device : devices) {
            if (device.getDeleted() == 1) {
                log.warn("【任务】设备已删除, deviceId={}", device.getId());
                throw new ResourceNotFoundException(StatusCode.DEVICE_NOT_FOUND);
            }
        }
    }


    private void createTaskAlgorithms(Long taskId, List<TaskAlgorithmConfigRequest> algorithmConfigs) {
        if (CollectionUtils.isEmpty(algorithmConfigs)) {
            return;
        }
        List<TaskAlgorithm> algorithmList = new ArrayList<>();
        List<TaskDevice> deviceList = new ArrayList<>();

        for (TaskAlgorithmConfigRequest config : algorithmConfigs) {
            Event event = new Event();
            event.setTaskId(taskId);
            event.setAlgorithmId(config.getAlgorithmId());
            event.setEventName(config.getEventName());
            event.setEventType(EventType.OUTPUT.getCode());
            event.setStatus(1);
            eventMapper.insert(event);

            TaskAlgorithm algo = new TaskAlgorithm();
            algo.setTaskId(taskId);
            algo.setAlgorithmId(config.getAlgorithmId());
            algo.setSampleInterval(config.getSampleInterval());
            algo.setValidityEnd(config.getValidityEnd());
            algo.setEventId(event.getId());
            algo.setScheduleRules(config.getScheduleRules());
            algorithmList.add(algo);

            if (!CollectionUtils.isEmpty(config.getDevices())) {
                for (TaskDeviceConfigRequest deviceConfig : config.getDevices()) {
                    TaskDevice td = new TaskDevice();
                    td.setTaskId(taskId);
                    td.setAlgorithmId(config.getAlgorithmId());
                    td.setDeviceId(deviceConfig.getDeviceId());
                    td.setEnabled(1);
                    deviceList.add(td);
                }
            }
        }

        taskAlgorithmMapper.insertBatch(algorithmList);
        if (!deviceList.isEmpty()) {
            taskDeviceMapper.insertBatch(deviceList);
        }

        eventCache.refreshTask(taskId);
    }


    private void updateTaskAlgorithms(Long taskId, List<TaskAlgorithmConfigRequest> algorithmConfigs) {
        if (CollectionUtils.isEmpty(algorithmConfigs)) {
            return;
        }

        List<TaskAlgorithm> existingAlgorithms = taskAlgorithmMapper.selectList(
                new LambdaQueryWrapper<TaskAlgorithm>()
                        .eq(TaskAlgorithm::getTaskId, taskId));
        Map<Long, TaskAlgorithm> existingAlgoMap = existingAlgorithms.stream()
                .collect(Collectors.toMap(TaskAlgorithm::getAlgorithmId, a -> a, (a1, a2) -> a1));

        List<Event> existingEvents = eventMapper.selectList(
                new LambdaQueryWrapper<Event>()
                        .eq(Event::getTaskId, taskId)
                        .eq(Event::getDeleted, 0));

        Set<Long> processedAlgorithmIds = new HashSet<>();
        Set<Long> retainedEventIds = new HashSet<>();

        for (TaskAlgorithmConfigRequest config : algorithmConfigs) {
            Long algorithmId = config.getAlgorithmId();
            processedAlgorithmIds.add(algorithmId);

            Long eventId = resolveEventId(taskId, config, existingEvents);
            retainedEventIds.add(eventId);

            TaskAlgorithm existAlgo = existingAlgoMap.get(algorithmId);
            if (existAlgo == null) {
                TaskAlgorithm algo = new TaskAlgorithm();
                algo.setTaskId(taskId);
                algo.setAlgorithmId(algorithmId);
                algo.setSampleInterval(config.getSampleInterval());
                algo.setValidityEnd(config.getValidityEnd());
                algo.setEventId(eventId);
                algo.setScheduleRules(config.getScheduleRules());
                taskAlgorithmMapper.insert(algo);
            } else {
                TaskAlgorithm algo = new TaskAlgorithm();
                algo.setId(existAlgo.getId());
                algo.setSampleInterval(config.getSampleInterval());
                algo.setValidityEnd(config.getValidityEnd());
                algo.setEventId(eventId);
                algo.setScheduleRules(config.getScheduleRules());
                taskAlgorithmMapper.updateById(algo);
            }

            updateTaskDevices(taskId, algorithmId, config.getDevices());
        }

        for (TaskAlgorithm existAlgo : existingAlgorithms) {
            if (!processedAlgorithmIds.contains(existAlgo.getAlgorithmId())) {
                taskAlgorithmMapper.deleteById(existAlgo.getId());
                taskDeviceMapper.delete(
                        new LambdaQueryWrapper<TaskDevice>()
                                .eq(TaskDevice::getTaskId, taskId)
                                .eq(TaskDevice::getAlgorithmId, existAlgo.getAlgorithmId()));
            }
        }

        for (Event event : existingEvents) {
            if (!retainedEventIds.contains(event.getId())) {
                eventMapper.deleteById(event.getId());
                log.debug("【任务】清理未使用的事件: taskId={}, eventId={}, eventName={}",
                        taskId, event.getId(), event.getEventName());
            }
        }

        eventCache.refreshTask(taskId);
    }


    private void updateTaskDevices(Long taskId, Long algorithmId, List<TaskDeviceConfigRequest> deviceConfigs) {
        List<TaskDevice> existingDevices = taskDeviceMapper.selectList(
                new LambdaQueryWrapper<TaskDevice>()
                        .eq(TaskDevice::getTaskId, taskId)
                        .eq(TaskDevice::getAlgorithmId, algorithmId));

        Set<Long> newDeviceIds = CollectionUtils.isEmpty(deviceConfigs)
                ? new HashSet<>()
                : deviceConfigs.stream().map(TaskDeviceConfigRequest::getDeviceId).collect(Collectors.toSet());

        Set<Long> existingDeviceIds = existingDevices.stream()
                .map(TaskDevice::getDeviceId)
                .collect(Collectors.toSet());

        List<TaskDevice> toAdd = new ArrayList<>();
        if (!CollectionUtils.isEmpty(deviceConfigs)) {
            for (TaskDeviceConfigRequest deviceConfig : deviceConfigs) {
                Long deviceId = deviceConfig.getDeviceId();
                if (!existingDeviceIds.contains(deviceId)) {
                    TaskDevice td = new TaskDevice();
                    td.setTaskId(taskId);
                    td.setAlgorithmId(algorithmId);
                    td.setDeviceId(deviceId);
                    td.setEnabled(1);
                    toAdd.add(td);
                }
            }
        }

        if (!toAdd.isEmpty()) {
            taskDeviceMapper.insertBatch(toAdd);
        }

        for (TaskDevice existDevice : existingDevices) {
            if (!newDeviceIds.contains(existDevice.getDeviceId())) {
                taskDeviceMapper.deleteById(existDevice.getId());
            }
        }
    }


    private Long resolveEventId(Long taskId, TaskAlgorithmConfigRequest config, List<Event> existingEvents) {
        if (config.getEventId() != null) {
            Event existEvent = existingEvents.stream()
                    .filter(e -> e.getId().equals(config.getEventId()))
                    .findFirst()
                    .orElse(null);

            if (existEvent != null) {
                boolean changed = false;
                if (!existEvent.getEventName().equals(config.getEventName())) {
                    existEvent.setEventName(config.getEventName());
                    changed = true;
                }
                if (!existEvent.getAlgorithmId().equals(config.getAlgorithmId())) {
                    existEvent.setAlgorithmId(config.getAlgorithmId());
                    changed = true;
                }
                if (changed) {
                    eventMapper.updateById(existEvent);
                }
                return config.getEventId();
            }
        }

        Event event = new Event();
        event.setTaskId(taskId);
        event.setAlgorithmId(config.getAlgorithmId());
        event.setEventName(config.getEventName());
        event.setEventType(EventType.OUTPUT.getCode());
        event.setStatus(1);
        eventMapper.insert(event);
        return event.getId();
    }


    private void enrichVO(TaskVO vo) {
        TaskStatus taskStatus = TaskStatus.fromCode(vo.getTaskStatus());
        vo.setTaskStatusDesc(taskStatus != null ? taskStatus.getDescription() : "未知");
        vo.setStatusDesc(vo.getStatus() != null && vo.getStatus() == 1 ? "启用" : "禁用");
    }


    private void enrichDeviceVO(TaskDeviceVO vo) {
        vo.setEnabled(Boolean.TRUE.equals(vo.getEnabled()));
    }


    private void validateAlgorithmConfigs(List<TaskAlgorithmConfigRequest> algorithmConfigs) {
        if (CollectionUtils.isEmpty(algorithmConfigs)) {
            return;
        }
        for (TaskAlgorithmConfigRequest config : algorithmConfigs) {
            validateScheduleRules(config.getScheduleRules());
        }
    }


    private void validateScheduleRules(List<ScheduleRule> scheduleRules) {
        if (CollectionUtils.isEmpty(scheduleRules)) {
            return;
        }

        for (ScheduleRule rule : scheduleRules) {
            if (CollectionUtils.isEmpty(rule.getDays()) || CollectionUtils.isEmpty(rule.getPeriods())) {
                continue;
            }
            if (hasOverlap(rule.getPeriods())) {
                log.warn("【任务】调度规则单条规则内时间段存在重叠");
                throw new ServiceException(StatusCode.TASK_SCHEDULE_OVERLAP.getMessage());
            }
        }

        Map<Integer, List<EffectivePeriod>> dayPeriodsMap = new HashMap<>();
        for (ScheduleRule rule : scheduleRules) {
            if (CollectionUtils.isEmpty(rule.getDays()) || CollectionUtils.isEmpty(rule.getPeriods())) {
                continue;
            }
            for (Integer day : rule.getDays()) {
                dayPeriodsMap.computeIfAbsent(day, k -> new ArrayList<>()).addAll(rule.getPeriods());
            }
        }

        for (Map.Entry<Integer, List<EffectivePeriod>> entry : dayPeriodsMap.entrySet()) {
            if (hasOverlap(entry.getValue())) {
                log.warn("【任务】调度规则星期{}的时间段存在重叠", entry.getKey());
                throw new ServiceException(StatusCode.TASK_SCHEDULE_OVERLAP.getMessage());
            }
        }
    }


    private boolean hasOverlap(List<EffectivePeriod> periods) {
        if (CollectionUtils.isEmpty(periods) || periods.size() < 2) {
            return false;
        }

        List<int[]> sorted = periods.stream()
                .map(this::timeToMinutes)
                .sorted(Comparator.comparingInt(p -> p[0]))
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size() - 1; i++) {
            int[] current = sorted.get(i);
            int[] next = sorted.get(i + 1);
            if (current[1] > next[0]) {
                return true;
            }
        }
        return false;
    }


    private int[] timeToMinutes(EffectivePeriod period) {
        String[] startParts = period.getStart().split(":");
        String[] endParts = period.getEnd().split(":");
        int start = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
        int end = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
        return new int[]{start, end};
    }
}
