package com.example.controller;

import com.example.dto.UserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        // Подготовка данных
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("John Doe");
        requestDto.setEmail("john@example.com");
        requestDto.setAge(30);

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();

        // Настройка мока
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(responseDto);

        // Выполнение и проверка
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())  // HTTP 201
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        // Проверка вызова сервиса
        verify(userService, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void createUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Неверные данные
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("");  // Пустое имя - невалидно
        requestDto.setEmail("invalid-email");  // Невалидный email

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());  // HTTP 400

        // Сервис не должен вызываться
        verify(userService, never()).createUser(any(UserRequestDto.class));
    }

    @Test
    void getUserById_UserExists_ReturnsOk() throws Exception {
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .build();

        when(userService.getUserById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getAllUsers_ReturnsList() throws Exception {
        List<UserResponseDto> users = Arrays.asList(
                UserResponseDto.builder().id(1L).name("User1").email("user1@test.com").build(),
                UserResponseDto.builder().id(2L).
                        name("User2").email("user2@test.com").build()
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void updateUser_ValidRequest_ReturnsOk() throws Exception {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Updated Name");
        requestDto.setEmail("updated@example.com");
        requestDto.setAge(35);

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Updated Name")
                .email("updated@example.com")
                .age(35)
                .build();

        when(userService.updateUser(eq(1L), any(UserRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(userService, times(1)).updateUser(eq(1L), any(UserRequestDto.class));
    }

    @Test
    void deleteUser_UserExists_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());  // HTTP 204

        verify(userService, times(1)).deleteUser(1L);
    }
}