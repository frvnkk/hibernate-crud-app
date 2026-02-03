package com.example.service;

import com.example.dto.UserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.entity.User;
import com.example.kafka.UserEventProducer;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private com.example.dto.UserMapper userMapper;  // Полный путь если нужно

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDto requestDto;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Создаем объекты без использования builder
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        requestDto = new UserRequestDto();
        requestDto.setName("John Doe");
        requestDto.setEmail("john@example.com");

        responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setName("John Doe");
        responseDto.setEmail("john@example.com");
        // responseDto.setAge(25); // если есть поле age
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRequestDto.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(responseDto);

        // Act
        UserResponseDto result = userService.createUser(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userEventProducer, times(1)).sendUserEvent(any());
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        // Act
        UserResponseDto result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(com.example.exception.UserNotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");

        UserResponseDto responseDto2 = new UserResponseDto();
        responseDto2.setId(2L);
        responseDto2.setName("Jane Doe");
        responseDto2.setEmail("jane@example.com");

        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(user)).thenReturn(responseDto);
        when(userMapper.toDto(user2)).thenReturn(responseDto2);

        // Act
        List<UserResponseDto> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Doe", result.get(1).getName());
    }

    @Test
    void updateUser_Success() {
        // Arrange
        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setName("John Updated");
        updateDto.setEmail("john.updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("John Updated");
        updatedUser.setEmail("john.updated@example.com");

        UserResponseDto updatedResponseDto = new UserResponseDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("John Updated");
        updatedResponseDto.setEmail("john.updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedResponseDto);

        // Act
        UserResponseDto result = userService.updateUser(1L, updateDto);

        // Assert
        assertEquals("John Updated", result.getName());
        assertEquals("john.updated@example.com", result.getEmail());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).delete(user);
        verify(userEventProducer, times(1)).sendUserEvent(any());
    }
}