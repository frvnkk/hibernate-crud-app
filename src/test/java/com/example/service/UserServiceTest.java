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
        // Подготовка
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail("existing@example.com");

        // Настройка мока
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Выполнение и проверка
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(requestDto));

        assertEquals("User with email 'existing@example.com' already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Подготовка
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

        // Настройка моков
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        // Выполнение
        UserResponseDto result = userService.getUserById(1L);

        // Проверка
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Настройка мока
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Выполнение и проверка
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserById(999L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getAllUsers_Success() {
        // Подготовка
        User user1 = User.builder().id(1L).name("User1").email("user1@test.com").age(25).build();
        User user2 = User.builder().id(2L).name("User2").email("user2@test.com").age(30).build();

        UserResponseDto dto1 = UserResponseDto.builder().id(1L).name("User1").email("user1@test.com").age(25).build();
        UserResponseDto dto2 = UserResponseDto.builder().id(2L).name("User2").email("user2@test.com").age(30).build();

        // Настройка моков
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        // Выполнение
        List<UserResponseDto> result = userService.getAllUsers();

        // Проверка
        assertEquals(2, result.size());
        assertEquals("User1", result.get(0).getName());
        assertEquals("User2", result.get(1).getName());

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toDto(user1);
        verify(userMapper, times(1)).toDto(user2);
    }

    @Test
    void updateUser_Success() {
        // Подготовка
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Updated Name");
        requestDto.setEmail("updated@example.com");
        requestDto.setAge(35);

        User existingUser = User.builder()
                .id(1L)
                .name("Original Name")
                .email("original@example.com")
                .age(30)
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .name("Updated Name")
                .email("updated@example.com")
                .age(35)
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Updated Name")
                .email("updated@example.com")
                .age(35)
                .build();

        // Настройка моков
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(responseDto);

        // Выполнение
        UserResponseDto result = userService.updateUser(1L, requestDto);

        // Проверка
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals(35, result.getAge());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("updated@example.com");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void deleteUser_Success() {
        // Настройка моков
        when(userRepository.existsById(1L)).thenReturn(true);

        // Выполнение
        userService.deleteUser(1L);

        // Проверка
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        // Настройка мока
        when(userRepository.existsById(999L)).thenReturn(false);

        // Выполнение и проверка
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(999L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
