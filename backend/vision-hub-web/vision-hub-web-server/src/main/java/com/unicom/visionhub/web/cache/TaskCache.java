package com.unicom.visionhub.web.cache;

import cn.hutool.core.util.StrUtil;
import com.unicom.visionhub.common.enums.TaskStatus;
import com.unicom.visionhub.web.entity.task.Task;
import com.unicom.visionhub.web.mapper.TaskMapper;
import com.unicom.visionhub.web.util.RedisUtil;
import com.unicom.visionhub.web.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCache {

    private static final String TASK_VO_KEY_PREFIX = "task:vo:";
    private static final long EXPIRE_SECONDS = 1800;

    private final RedisUtil redisUtil;
    private final TaskMapper taskMapper;


    public TaskVO getTaskVO(Long taskId) {
        if (taskId == null) {
            return null;
        }

        String key = TASK_VO_KEY_PREFIX + taskId;

        TaskVO cached = redisUtil.get(key);
        if (cached != null) {
            log.debug("【TaskCache】缓存命中: id={}", taskId);
            return cached;
        }

        log.debug("【TaskCache】缓存未命中: id={}", taskId);
        TaskVO vo = taskMapper.selectVOById(taskId);
        if (vo == null) {
            return null;
        }

        enrichVO(vo);
        redisUtil.set(key, vo, EXPIRE_SECONDS, TimeUnit.SECONDS);

        return vo;
    }


    public Task getTask(Long taskId) {
        if (taskId == null) {
            return null;
        }

        String key = TASK_VO_KEY_PREFIX + taskId;

        TaskVO cached = redisUtil.get(key);
        if (cached != null) {
            Task task = new Task();
            task.setId(cached.getId());
            task.setTaskName(cached.getTaskName());
            task.setTaskStatus(cached.getTaskStatus());
            task.setStatus(cached.getStatus());
            task.setRemark(cached.getRemark());
            task.setDeleted(0);
            return task;
        }

        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted() == 1) {
            return null;
        }

        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setTaskStatus(task.getTaskStatus());
        vo.setStatus(task.getStatus());
        vo.setRemark(task.getRemark());
        enrichVO(vo);
        redisUtil.set(key, vo, EXPIRE_SECONDS, TimeUnit.SECONDS);

        return task;
    }


    public void evictTask(Long taskId) {
        if (taskId == null) {
            return;
        }
        redisUtil.delete(TASK_VO_KEY_PREFIX + taskId);
        log.debug("【TaskCache】移除任务缓存: id={}", taskId);
    }


    private void enrichVO(TaskVO vo) {
        TaskStatus taskStatus = TaskStatus.fromCode(vo.getTaskStatus());
        vo.setTaskStatusDesc(taskStatus != null ? taskStatus.getDescription() : "未知");
        vo.setStatusDesc(vo.getStatus() != null && vo.getStatus() == 1 ? "启用" : "禁用");
    }
}