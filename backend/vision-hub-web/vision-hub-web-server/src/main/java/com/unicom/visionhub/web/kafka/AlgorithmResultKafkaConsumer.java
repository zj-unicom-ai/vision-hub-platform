package com.unicom.visionhub.web.kafka;

import cn.hutool.json.JSONUtil;
import com.unicom.visionhub.web.cache.EventCache;
import com.unicom.visionhub.common.enums.EventRecordStatus;
import com.unicom.visionhub.web.dto.AlgorithmResultMessage;
import com.unicom.visionhub.web.entity.event.Event;
import com.unicom.visionhub.web.entity.event.EventRecord;
import com.unicom.visionhub.web.service.EventRecordService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
@Component
@RequiredArgsConstructor
public class AlgorithmResultKafkaConsumer {

    private final EventRecordService eventRecordService;
    private final EventCache eventCache;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private final AtomicBoolean firstConsume = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        log.info("【Kafka】Consumer 初始化完成, bootstrapServers={}, groupId={}, topic={}",
                bootstrapServers, groupId, AlgorithmResultKafkaProducer.TOPIC_ALGORITHM_RESULT);
    }

    @KafkaListener(
            topics = AlgorithmResultKafkaProducer.TOPIC_ALGORITHM_RESULT,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(String messageJson) {
        if (firstConsume.compareAndSet(true, false)) {
            log.info("【Kafka】Consumer 开始消费消息, topic={}, groupId={}",
                    AlgorithmResultKafkaProducer.TOPIC_ALGORITHM_RESULT, groupId);
        }
        log.debug("【Kafka】收到算法分析结果消息: {}", truncateMessage(messageJson));

        try {
            AlgorithmResultMessage message = JSONUtil.toBean(messageJson, AlgorithmResultMessage.class);

            List<Event> events = eventCache.getEvents(message.getTaskId(), message.getAlgorithmId());
            if (events.isEmpty()) {
                log.debug("【Kafka】任务算法下无事件定义，跳过: taskId={}, algorithmId={}",
                        message.getTaskId(), message.getAlgorithmId());
                return;
            }

            for (Event event : events) {
                EventRecord eventRecord = new EventRecord();
                eventRecord.setTaskId(message.getTaskId());
                eventRecord.setDeviceId(message.getDeviceId());
                eventRecord.setAlgorithmId(message.getAlgorithmId());
                eventRecord.setEventId(event.getId());
                eventRecord.setEventName(event.getEventName());
                eventRecord.setEventType(event.getEventType());
                eventRecord.setAlarmImage(message.getResultImage());
                eventRecord.setAnalysisResult(message.getAnalysisResult());
                eventRecord.setExecuteDuration(message.getExecuteDuration());
                eventRecord.setConfidence(message.getConfidence());
                eventRecord.setStatus(EventRecordStatus.PENDING.getCode());
                eventRecordService.save(eventRecord);
            }

            log.info("【Kafka】事件记录保存成功: taskId={}, deviceId={}, algorithmId={}, eventCount={}",
                    message.getTaskId(), message.getDeviceId(), message.getAlgorithmId(), events.size());
        } catch (Exception e) {
            log.error("【Kafka】处理算法分析结果消息失败: {}", truncateMessage(messageJson), e);
        }
    }


    private String truncateMessage(String message) {
        if (message == null || message.length() <= 2000) {
            return message;
        }
        return message.substring(0, 2000) + "...[已截断，总长度" + message.length() + "字节]";
    }
}
