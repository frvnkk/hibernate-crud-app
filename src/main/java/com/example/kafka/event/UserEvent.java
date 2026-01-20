package com.example.kafka.event;

import java.time.LocalDateTime;

public class UserEvent {
    private String eventType; // USER_CREATED, USER_DELETED
    private String email;
    private Long userId;
    private LocalDateTime timestamp;
    private String username;

    public UserEvent() {}

    public UserEvent(String eventType, String email, Long userId, String username) {
        this.eventType = eventType;
        this.email = email;
        this.userId = userId;
        this.username = username;
        this.timestamp = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}