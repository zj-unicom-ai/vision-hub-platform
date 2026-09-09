package com.unicom.visionhub.web.task;

import com.unicom.visionhub.web.mapper.AlgorithmCallLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "algorithm.call.log.clean", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AlgorithmCallLogCleanTask {

    private final AlgorithmCallLogMapper algorithmCallLogMapper;


    @Scheduled(cron = "${algorithm.call.log.clean.cron:0 0 2 * * ?}")
    public void cleanCallLog() {
        int retainDays = 30;
        log.info("【定时任务】开始清理 {} 天前的算法调用记录", retainDays);

        int deletedCount = algorithmCallLogMapper.deleteOldRecords(retainDays);
        log.info("【定时任务】算法调用记录清理完成，删除 {} 条记录", deletedCount);
    }
}