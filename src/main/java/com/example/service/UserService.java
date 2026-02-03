package com.example.service;

import com.example.dto.UserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.dto.UserMapper;
import com.example.entity.User;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.kafka.UserEventProducer;
import com.example.kafka.event.UserEvent;
import com.example.kafka.event.EventType;
import com.example.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventProducer userEventProducer;
    private final DiscoveryClient discoveryClient; // Для Service Discovery
    private final RestTemplate restTemplate; // Для вызовов других сервисов

    @Transactional
    public UserResponseDto createUser(UserRequestDto requestDto) {
        log.info("Creating user: {} - {}", requestDto.getName(), requestDto.getEmail());

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UserAlreadyExistsException(requestDto.getEmail());
        }

        User user = userMapper.toEntity(requestDto);
        User savedUser = userRepository.save(user);

        // Отправка события в Kafka о создании пользователя
        UserEvent event = new UserEvent(
                EventType.USER_CREATED,
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getName()
        );
        userEventProducer.sendUserEvent(event);
        log.info("Sent USER_CREATED event to Kafka for user: {}", savedUser.getEmail());

        // Пример вызова другого сервиса с Circuit Breaker
        callNotificationServiceWithCircuitBreaker(savedUser);

        return userMapper.toDto(savedUser);
    }

    // Метод с Circuit Breaker и Retry
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackCallNotificationService")
    @Retry(name = "notificationService", fallbackMethod = "fallbackCallNotificationService")
    public void callNotificationServiceWithCircuitBreaker(User user) {
        // Используем Service Discovery для получения URL notification-service
        List<ServiceInstance> instances = discoveryClient.getInstances("notification-service");

        if (!instances.isEmpty()) {
            ServiceInstance instance = instances.get(0);
            String url = instance.getUri() + "/api/notifications/welcome/" + user.getId();

            try {
                String response = restTemplate.getForObject(url, String.class);
                log.info("Notification service response: {}", response);
            } catch (Exception e) {
                log.error("Error calling notification service: {}", e.getMessage());
                throw e; // Пробрасываем исключение для Circuit Breaker
            }
        } else {
            log.warn("Notification service not available in service discovery");
        }
    }

    // Fallback метод для Circuit Breaker
    public void fallbackCallNotificationService(User user, Throwable t) {
        log.warn("Fallback triggered for notification service call for user: {}. Error: {}",
                user.getEmail(), t.getMessage());

    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Отправка события в Kafka об удалении пользователя
        UserEvent event = new UserEvent(
                EventType.USER_DELETED,
                user.getEmail(),
                user.getId(),
                user.getName()
        );
        userEventProducer.sendUserEvent(event);
        log.info("Sent USER_DELETED event to Kafka for user: {}", user.getEmail());

        // Вызов notification service с Circuit Breaker для уведомления об удалении
        try {
            callNotificationServiceForDeletion(user);
        } catch (Exception e) {
            log.error("Error notifying about user deletion, but continuing with delete operation", e);
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getEmail());
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackNotificationForDeletion")
    private void callNotificationServiceForDeletion(User user) {
        List<ServiceInstance> instances = discoveryClient.getInstances("notification-service");

        if (!instances.isEmpty()) {
            ServiceInstance instance = instances.get(0);
            String url = instance.getUri() + "/api/notifications/goodbye/" + user.getId();

            String response = restTemplate.postForObject(url, null, String.class);
            log.info("Deletion notification sent: {}", response);
        }
    }

    private void fallbackNotificationForDeletion(User user, Throwable t) {
        log.warn("Could not send deletion notification for user: {}. Will retry later. Error: {}",
                user.getEmail(), t.getMessage());
        // Можно сохранить в таблицу failed_notifications для повторной попытки
    }

    // Дополнительный метод для проверки health через Service Discovery
    public boolean isNotificationServiceAvailable() {
        List<ServiceInstance> instances = discoveryClient.getInstances("notification-service");
        return !instances.isEmpty();
    }

    // Получение информации о всех сервисах из Service Discovery
    public List<String> getAvailableServices() {
        return discoveryClient.getServices();
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }
}