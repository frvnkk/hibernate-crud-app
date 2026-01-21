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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return userMapper.toDto(savedUser);
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

        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getEmail());
    }

    // Эти методы остаются без изменений
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