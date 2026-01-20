package com.example.kafka;

import com.example.kafka.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventProducer.class);

    @Value("${kafka.topic.user-events:user-events}")
    private String userEventsTopic;

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserEvent(UserEvent event) {
        try {
            kafkaTemplate.send(userEventsTopic, event);
            logger.info("Отправлено событие пользователя в Kafka: {}", event);
        } catch (Exception e) {
            logger.error("Не удалось отправить событие пользователя в Kafka: {}", e.getMessage());
        }
    }
}