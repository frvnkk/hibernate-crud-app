package com.example.service;

import com.example.dto.UserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.dto.UserMapper;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        // Подготовка
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("John Doe");
        requestDto.setEmail("john@example.com");
        requestDto.setAge(30);

        User user = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();

        // Настройка моков
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(responseDto);

        // Выполнение
        UserResponseDto result = userService.createUser(requestDto);

        // Проверка
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(requestDto));

        assertEquals("User with email existing@example.com already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_Success() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getAllUsers_Success() {
        User user1 = User.builder().id(1L).name("User1").build();
        User user2 = User.builder().
                id(2L).name("User2").build();

        UserResponseDto dto1 = UserResponseDto.builder().id(1L).name("User1").build();
        UserResponseDto dto2 = UserResponseDto.builder().id(2L).name("User2").build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        List<UserResponseDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }
}