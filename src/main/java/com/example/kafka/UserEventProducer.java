package com.example.kafka;

import com.example.kafka.event.UserEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UserEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendUserEvent(UserEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(userEventsTopic, message);
            logger.info("Отправлено событие в Kafka: {}", message);
        } catch (JsonProcessingException e) {
            logger.error("Ошибка сериализации события: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка отправки в Kafka: {}", e.getMessage());
        }
    }
}