package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateUser_Success() {
        // Given
        when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(testUser);

        // When
        User createdUser = userService.createUser("Test User", "test@example.com", 25);

        // Then
        assertNotNull(createdUser);
        assertEquals("Test User", createdUser.getName());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals(25, createdUser.getAge());

        verify(userDao, times(1)).findByEmail("test@example.com");
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        // Given
        when(userDao.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser("New User", "existing@example.com", 30);
        });

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userDao, times(1)).findByEmail("existing@example.com");
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_Success() {
        // Given
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> foundUser = userService.getUserById(1L);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> foundUser = userService.getUserById(999L);

        // Then
        assertFalse(foundUser.isPresent());
        verify(userDao, times(1)).findById(999L);
    }

    @Test
    void testGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(
                testUser,
                User.builder().id(2L).name("User2").email("user2@test.com").age(30).build()
        );
        when(userDao.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userService.updateUser(1L, "Updated Name", "updated@example.com", 30);

        // Then
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).update(any(User.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        // Given
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(999L, "New Name", "new@example.com", 30);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userDao, times(1)).findById(999L);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void testDeleteUser() {
        // Given
        doNothing().when(userDao).delete(1L);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userDao, times(1)).delete(1L);
    }
}