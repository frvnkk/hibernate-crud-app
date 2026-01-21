package com.example.dto;

import com.example.kafka.event.EventType;
import java.time.LocalDateTime;

public class UserEventDto {
    private EventType eventType;
    private String email;
    private Long userId;
    private LocalDateTime timestamp;
    private String username;

    // Конструкторы, геттеры, сеттеры
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}