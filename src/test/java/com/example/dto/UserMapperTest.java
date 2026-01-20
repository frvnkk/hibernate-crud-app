package com.example.dto;

import com.example.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toDto_ConvertsUserToDto() {
        // Подготовка
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 30))
                .build();

        // Выполнение
        UserResponseDto dto = userMapper.toDto(user);

        // Проверка
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals(30, dto.getAge());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 30), dto.getCreatedAt());
    }

    @Test
    void toEntity_ConvertsDtoToUser() {
        // Подготовка
        UserRequestDto dto = new UserRequestDto();
        dto.setName("Jane Doe");
        dto.setEmail("jane@example.com");
        dto.setAge(25);

        // Выполнение
        User user = userMapper.toEntity(dto);

        // Проверка
        assertNotNull(user);
        assertNull(user.getId());  // ID не должен быть установлен при создании
        assertEquals("Jane Doe", user.getName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals(25, user.getAge());
        assertNull(user.getCreatedAt());  // CreatedAt установится при сохранении
    }

    @Test
    void toEntity_NullDto_ReturnsNull() {
        // Проверяем, что метод не падает на null
        User result = userMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toDto_NullUser_ReturnsNull() {
        // Проверяем, что метод не падает на null
        UserResponseDto result = userMapper.toDto(null);
        assertNull(result);
    }

    @Test
    void toEntity_EmptyDto_ReturnsUserWithNullFields() {
        // Подготовка: DTO с null полями
        UserRequestDto dto = new UserRequestDto();
        // Поля не установлены - будут null

        // Выполнение
        User user = userMapper.toEntity(dto);

        // Проверка
        assertNotNull(user);
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getAge());
    }

    @Test
    void toDto_UserWithNullFields_ReturnsDtoWithNullFields() {
        // Подготовка: User с null полями
        User user = new User();
        user.setId(1L);
        // Остальные поля null

        // Выполнение
        UserResponseDto dto = userMapper.toDto(user);

        // Проверка
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getEmail());
        assertNull(dto.getAge());
        assertNull(dto.getCreatedAt());
    }
}