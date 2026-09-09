package com.unicom.visionhub.web.task;

import com.unicom.visionhub.common.enums.TaskStatus;
import com.unicom.visionhub.web.cache.TaskCache;
import com.unicom.visionhub.web.cache.TaskAlgorithmCache;
import com.unicom.visionhub.web.entity.task.Task;
import com.unicom.visionhub.web.entity.task.TaskAlgorithm;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;


@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAnalysisEngine {

    private final ThreadPoolTaskScheduler taskScheduler;
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


    private final Map<String, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        List<Task> runningTasks = taskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                        .eq(Task::getTaskStatus, TaskStatus.RUNNING.getCode())
                        .eq(Task::getDeleted, 0));

        if (CollectionUtils.isEmpty(runningTasks)) {
            log.info("【任务分析】应用启动，没有运行中的任务需要恢复");
            return;
        }

        log.info("【任务分析】应用启动，恢复 {} 个运行中的任务", runningTasks.size());
        for (Task task : runningTasks) {
            try {
                startTask(task.getId());
            } catch (Exception e) {
                log.error("【任务分析】恢复任务失败: taskId={}", task.getId(), e);
            }
        }
    }


    public void startTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted() == 1) {
            log.warn("【任务分析】启动任务分析失败，任务不存在或已删除: taskId={}", taskId);
            return;
        }

        List<TaskAlgorithm> algorithms = taskAlgorithmMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskAlgorithm>()
                        .eq(TaskAlgorithm::getTaskId, taskId));

        if (CollectionUtils.isEmpty(algorithms)) {
            log.warn("【任务分析】任务未配置算法，无需启动分析: taskId={}", taskId);
            return;
        }

        for (TaskAlgorithm algorithm : algorithms) {
            String taskKey = buildTaskKey(taskId, algorithm.getAlgorithmId());

            if (runningTasks.containsKey(taskKey)) {
                stopAlgorithmTask(taskKey);
            }

            long intervalMs = algorithm.getSampleInterval() != null
                    ? algorithm.getSampleInterval()
                    : 10000L;

            TaskAnalysisJob job = new TaskAnalysisJob(
                    taskId, algorithm.getAlgorithmId(),
                    taskMapper, taskAlgorithmMapper, taskDeviceMapper, deviceMapper,
                    algorithmService, frameCaptureService, kafkaProducer,
                    taskCache, taskAlgorithmCache,
                    minioService, taskDeviceAnalysisExecutor);

            ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(
                    job, Duration.ofMillis(intervalMs));

            runningTasks.put(taskKey, future);
            log.info("【任务分析】启动任务分析: taskId={}, algorithmId={}, interval={}ms",
                    taskId, algorithm.getAlgorithmId(), intervalMs);
        }
    }


    public void stopTask(Long taskId) {
        List<TaskAlgorithm> algorithms = taskAlgorithmMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskAlgorithm>()
                        .eq(TaskAlgorithm::getTaskId, taskId));

        if (CollectionUtils.isEmpty(algorithms)) {
            log.info("【任务分析】任务未配置算法，无需停止: taskId={}", taskId);
            return;
        }

        for (TaskAlgorithm algorithm : algorithms) {
            String taskKey = buildTaskKey(taskId, algorithm.getAlgorithmId());
            stopAlgorithmTask(taskKey);
        }

        log.info("【任务分析】停止任务分析完成: taskId={}", taskId);
    }


    public boolean isRunning(Long taskId, Long algorithmId) {
        String taskKey = buildTaskKey(taskId, algorithmId);
        ScheduledFuture<?> future = runningTasks.get(taskKey);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    private void stopAlgorithmTask(String taskKey) {
        ScheduledFuture<?> future = runningTasks.remove(taskKey);
        if (future != null) {
            future.cancel(false);
            log.debug("【任务分析】停止定时任务: {}", taskKey);
        }
    }

    private String buildTaskKey(Long taskId, Long algorithmId) {
        return "task:" + taskId + ":alg:" + algorithmId;
    }
}
