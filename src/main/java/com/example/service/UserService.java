package com.example.service;

import com.example.dto.UserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.dto.UserMapper;
import com.example.entity.User;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDto createUser(UserRequestDto requestDto) {
        log.info("Creating user: {}, {}", requestDto.getName(), requestDto.getEmail());

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UserAlreadyExistsException(requestDto.getEmail());
        }

        User user = userMapper.toEntity(requestDto);
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    public UserResponseDto getUserById(Long id) {
        log.info("Getting user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toDto(user);
    }

    public List<UserResponseDto> getAllUsers() {
        log.info("Getting all users");

        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto requestDto) {
        log.info("Updating user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equals(requestDto.getEmail()) &&
                userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UserAlreadyExistsException(requestDto.getEmail());
        }

        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());
        user.setAge(requestDto.getAge());

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }
}