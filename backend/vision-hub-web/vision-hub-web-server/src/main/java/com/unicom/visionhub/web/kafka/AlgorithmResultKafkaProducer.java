package com.unicom.visionhub.web.kafka;

import cn.hutool.json.JSONUtil;
import com.unicom.visionhub.web.dto.AlgorithmResultMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AlgorithmResultKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public static final String TOPIC_ALGORITHM_RESULT = "algorithm-result";

    @PostConstruct
    public void init() {
        log.info("【Kafka】Producer 初始化完成, bootstrapServers={}, topic={}",
                bootstrapServers, TOPIC_ALGORITHM_RESULT);
    }


    public void send(AlgorithmResultMessage message) {
        String json = JSONUtil.toJsonStr(message);
        kafkaTemplate.send(TOPIC_ALGORITHM_RESULT, String.valueOf(message.getTaskId()), json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("【Kafka】发送算法分析结果失败: taskId={}, deviceId={}",
                                message.getTaskId(), message.getDeviceId(), ex);
                    } else {
                        log.debug("【Kafka】发送算法分析结果成功: taskId={}, deviceId={}, partition={}",
                                message.getTaskId(), message.getDeviceId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
