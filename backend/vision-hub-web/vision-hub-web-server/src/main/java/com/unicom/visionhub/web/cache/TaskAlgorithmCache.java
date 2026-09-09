package com.unicom.visionhub.web.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.visionhub.web.entity.task.TaskAlgorithm;
import com.unicom.visionhub.web.mapper.TaskAlgorithmMapper;
import com.unicom.visionhub.web.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAlgorithmCache {

    private static final String TASK_ALGORITHM_VO_KEY_PREFIX = "task:algorithm:vo:";
    private static final long EXPIRE_SECONDS = 1800;

    private final RedisUtil redisUtil;
    private final TaskAlgorithmMapper taskAlgorithmMapper;


    public TaskAlgorithm getTaskAlgorithm(Long taskId, Long algorithmId) {
        if (taskId == null || algorithmId == null) {
            return null;
        }

        String key = TASK_ALGORITHM_VO_KEY_PREFIX + taskId + ":" + algorithmId;

        TaskAlgorithm cached = redisUtil.get(key);
        if (cached != null) {
            log.debug("【TaskAlgorithmCache】缓存命中: taskId={}, algorithmId={}", taskId, algorithmId);
            return cached;
        }

        log.debug("【TaskAlgorithmCache】缓存未命中: taskId={}, algorithmId={}", taskId, algorithmId);
        TaskAlgorithm taskAlgorithm = taskAlgorithmMapper.selectOne(
                new LambdaQueryWrapper<TaskAlgorithm>()
                        .eq(TaskAlgorithm::getTaskId, taskId)
                        .eq(TaskAlgorithm::getAlgorithmId, algorithmId));

        if (taskAlgorithm != null) {
            redisUtil.set(key, taskAlgorithm, EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        return taskAlgorithm;
    }


    public void evictTaskAlgorithm(Long taskId, Long algorithmId) {
        if (taskId == null || algorithmId == null) {
            return;
        }
        redisUtil.delete(TASK_ALGORITHM_VO_KEY_PREFIX + taskId + ":" + algorithmId);
        log.debug("【TaskAlgorithmCache】移除缓存: taskId={}, algorithmId={}", taskId, algorithmId);
    }


    public void evictByTaskId(Long taskId) {
        if (taskId == null) {
            return;
        }
        String pattern = TASK_ALGORITHM_VO_KEY_PREFIX + taskId + ":*";
        Iterable<String> keys = redisUtil.keys(pattern);
        if (keys != null) {
            for (String key : keys) {
                redisUtil.delete(key);
            }
        }
        log.debug("【TaskAlgorithmCache】清除任务所有算法缓存: taskId={}", taskId);
    }
}