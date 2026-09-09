package com.unicom.visionhub.web.task;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.unicom.visionhub.common.enums.AlgorithmExecStatus;
import com.unicom.visionhub.common.enums.TaskStatus;
import com.unicom.visionhub.web.dto.AlgorithmResultMessage;
import com.unicom.visionhub.web.dto.EffectivePeriod;
import com.unicom.visionhub.web.dto.ScheduleRule;
import com.unicom.visionhub.web.dto.request.AlgorithmExecuteRequest;
import com.unicom.visionhub.web.dto.response.AlgorithmExecuteResponse;
import com.unicom.visionhub.web.entity.algorithm.Algorithm;
import com.unicom.visionhub.web.entity.device.Device;
import com.unicom.visionhub.web.entity.task.Task;
import com.unicom.visionhub.web.entity.task.TaskAlgorithm;
import com.unicom.visionhub.web.entity.task.TaskDevice;
import com.unicom.visionhub.web.cache.TaskCache;
import com.unicom.visionhub.web.cache.TaskAlgorithmCache;
import com.unicom.visionhub.web.kafka.AlgorithmResultKafkaProducer;
import com.unicom.visionhub.web.mapper.DeviceMapper;
import com.unicom.visionhub.web.mapper.TaskAlgorithmMapper;
import com.unicom.visionhub.web.mapper.TaskDeviceMapper;
import com.unicom.visionhub.web.mapper.TaskMapper;
import com.unicom.visionhub.web.service.AlgorithmService;
import com.unicom.visionhub.web.service.FrameCaptureService;
import com.unicom.visionhub.web.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


@Slf4j
@RequiredArgsConstructor
public class TaskAnalysisJob implements Runnable {

    private final Long taskId;
    private final Long algorithmId;
    private final TaskMapper taskMapper;
    private final TaskAlgorithmMapper taskAlgorithmMapper;
    private final TaskDeviceMapper taskDeviceMapper;
    private final DeviceMapper deviceMapper;
    private final AlgorithmService algorithmService;
    private final FrameCaptureService frameCaptureService;
    private final AlgorithmResultKafkaProducer kafkaProducer;
    private final TaskCache taskCache;
    private final TaskAlgorithmCache taskAlgorithmCache;
    private final MinioService minioService;
    private final Executor taskDeviceAnalysisExecutor;

    @Override
    public void run() {
        try {
            executeAnalysis();
        } catch (Exception e) {
            log.error("【任务分析】任务分析执行异常: taskId={}, algorithmId={}", taskId, algorithmId, e);
        }
    }

    private void executeAnalysis() {
        Task task = taskCache.getTask(taskId);
        if (task == null || Boolean.TRUE.equals(task.getDeleted())) {
            log.warn("【任务分析】任务不存在或已删除，跳过执行: taskId={}", taskId);
            return;
        }
        if (!TaskStatus.RUNNING.getCode().equals(task.getTaskStatus())) {
            log.debug("【任务分析】任务状态非运行中，跳过执行: taskId={}, status={}", taskId, task.getTaskStatus());
            return;
        }

        TaskAlgorithm taskAlgorithm = taskAlgorithmCache.getTaskAlgorithm(taskId, algorithmId);
        if (taskAlgorithm == null) {
            log.warn("【任务分析】任务算法配置不存在，跳过执行: taskId={}, algorithmId={}", taskId, algorithmId);
            return;
        }

        if (taskAlgorithm.getValidityEnd() != null && LocalDateTime.now().isAfter(taskAlgorithm.getValidityEnd())) {
            log.debug("【任务分析】算法配置已过有效期，跳过执行: taskId={}, algorithmId={}", taskId, algorithmId);
            return;
        }

        if (!isScheduleEffective(taskAlgorithm.getScheduleRules())) {
            log.debug("【任务分析】当前不在调度规则生效时间内，跳过执行: taskId={}, algorithmId={}", taskId, algorithmId);
            return;
        }

        Algorithm algorithm = algorithmService.getById(algorithmId);
        if (algorithm == null || algorithm.getDeleted() == 1) {
            log.warn("【任务分析】算法不存在或已删除，跳过执行: algorithmId={}", algorithmId);
            return;
        }

        List<TaskDevice> taskDevices = taskDeviceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskDevice>()
                        .eq(TaskDevice::getTaskId, taskId)
                        .eq(TaskDevice::getAlgorithmId, algorithmId));
        if (CollectionUtils.isEmpty(taskDevices)) {
            log.debug("【任务分析】任务算法未关联设备，跳过执行: taskId={}, algorithmId={}", taskId, algorithmId);
            return;
        }

        List<CompletableFuture<Void>> futures = taskDevices.stream()
                .map(taskDevice -> CompletableFuture.runAsync(() -> {
                    try {
                        analyzeDevice(task, algorithm, taskDevice);
                    } catch (Exception e) {
                        log.error("【任务分析】设备分析异常: taskId={}, deviceId={}",
                                taskId, taskDevice.getDeviceId(), e);
                    }
                }, taskDeviceAnalysisExecutor))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(60, TimeUnit.SECONDS)
                    .join();
            log.debug("【任务分析】本轮设备分析全部完成: taskId={}, algorithmId={}, deviceCount={}",
                    taskId, algorithmId, taskDevices.size());
        } catch (Exception e) {
            log.error("【任务分析】等待设备分析完成超时或异常: taskId={}, algorithmId={}",
                    taskId, algorithmId, e);
        }
    }

    private void analyzeDevice(Task task, Algorithm algorithm, TaskDevice taskDevice) {
        Device device = deviceMapper.selectById(taskDevice.getDeviceId());
        if (device == null || device.getDeleted() == 1) {
            log.warn("【任务分析】设备不存在或已删除，跳过: deviceId={}", taskDevice.getDeviceId());
            return;
        }
        if (device.getStatus() != null && device.getStatus() == 0) {
            log.debug("【任务分析】设备已禁用，跳过: deviceId={}", taskDevice.getDeviceId());
            return;
        }

        String snapshotObjectName = frameCaptureService.captureByDevice(device.getId(), device.getStreamUrl());
        if (StrUtil.isBlank(snapshotObjectName)) {
            log.warn("【任务分析】截帧失败，跳过分析: taskId={}, deviceId={}, streamUrl={}",
                    task.getId(), device.getId(), device.getStreamUrl());
            return;
        }

        AlgorithmExecuteRequest request = new AlgorithmExecuteRequest();
        request.setAlgorithmCode(algorithm.getAlgorithmCode());
        request.setInputImageUrl(minioService.getFileUrl(snapshotObjectName));
        request.setInputImage(snapshotObjectName);

        AlgorithmExecuteResponse response = algorithmService.executeForTask(request, task.getId(), device.getId());

        if (!AlgorithmExecStatus.SUCCESS.getCode().equals(response.getStatus())) {
            log.warn("【任务分析】算法执行失败，不生成事件记录: taskId={}, deviceId={}, error={}",
                    task.getId(), device.getId(), response.getErrorMessage());
            return;
        }

        if (!isAlarm(response.getOutputData())) {
            log.debug("【任务分析】算法未产生告警，不生成事件记录: taskId={}, deviceId={}",
                    task.getId(), device.getId());
            return;
        }

        AlgorithmResultMessage message = new AlgorithmResultMessage();
        message.setTaskId(task.getId());
        message.setDeviceId(device.getId());
        message.setAlgorithmId(algorithm.getId());
        message.setResultImage(response.getResultImage());
        message.setExecuteDuration(response.getExecuteDuration());
        message.setAnalysisResult(JSONUtil.toJsonStr(response.getOutputData()));

        Map<String, Object> outputData = response.getOutputData();
        if (outputData != null) {
            Object resultObj = outputData.get("result");
            if (resultObj instanceof Map<?, ?> resultMap) {
                Object dataObj = resultMap.get("data");
                if (dataObj instanceof Map<?, ?> dataMap) {
                    Object confidenceObj = dataMap.get("confidence");
                    if (confidenceObj != null) {
                        try {
                            message.setConfidence(new BigDecimal(confidenceObj.toString()));
                        } catch (Exception e) {
                            log.warn("【任务分析】解析 confidence 失败: taskId={}, deviceId={}, confidence={}",
                                    task.getId(), device.getId(), confidenceObj);
                        }
                    }
                }
            }
        }

        kafkaProducer.send(message);

        log.info("【任务分析】告警结果已发送 Kafka: taskId={}, deviceId={}, algorithmId={}",
                task.getId(), device.getId(), algorithm.getId());
    }


    @SuppressWarnings("unchecked")
    private boolean isAlarm(Map<String, Object> outputData) {
        if (outputData == null) {
            return false;
        }
        Object resultObj = outputData.get("result");
        if (!(resultObj instanceof Map<?, ?> resultMap)) {
            return false;
        }
        Object dataObj = resultMap.get("data");
        if (!(dataObj instanceof Map<?, ?> dataMap)) {
            return false;
        }
        Object isAlarmObj = dataMap.get("isAlarm");
        if (isAlarmObj instanceof Boolean alarm) {
            return alarm;
        }
        if (isAlarmObj instanceof String alarm) {
            return Boolean.parseBoolean(alarm);
        }
        return false;
    }


    private boolean isScheduleEffective(List<ScheduleRule> scheduleRules) {
        if (CollectionUtils.isEmpty(scheduleRules)) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        int currentDay = now.getDayOfWeek().getValue();
        int currentMinutes = now.getHour() * 60 + now.getMinute();

        for (ScheduleRule rule : scheduleRules) {
            if (rule.getDays() != null && !rule.getDays().contains(currentDay)) {
                continue;
            }
            if (rule.getPeriods() != null) {
                for (EffectivePeriod period : rule.getPeriods()) {
                    int start = timeToMinutes(period.getStart());
                    int end = timeToMinutes(period.getEnd());
                    if (currentMinutes >= start && currentMinutes <= end) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

}
