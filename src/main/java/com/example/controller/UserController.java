package com.example.controller;

import com.example.dto.UserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список пользователей",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();

        // Добавляем ссылки к каждому пользователю
        List<UserResponseDto> usersWithLinks = users.stream()
                .map(user -> user.add(
                        linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel(),
                        linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"),
                        linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete")
                ))
                .collect(Collectors.toList());

        // Создаем коллекцию с ссылками
        CollectionModel<UserResponseDto> collectionModel = CollectionModel.of(usersWithLinks);

        // Добавляем ссылку на создание нового пользователя
        collectionModel.add(linkTo(methodOn(UserController.class).createUser(null)).withRel("create"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id) {

        UserResponseDto user = userService.getUserById(id);

        // Добавляем HATEOAS ссылки
        user.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        user.add(linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"));
        user.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        user.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Создать нового пользователя",
            description = "Создает нового пользователя и отправляет событие в Kafka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Неверные данные пользователя",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Parameter(description = "Данные нового пользователя", required = true)
            @Valid @RequestBody UserRequestDto requestDto) {

        UserResponseDto createdUser = userService.createUser(requestDto);

        // Добавляем HATEOAS ссылки
        createdUser.add(linkTo(methodOn(UserController.class).getUserById(createdUser.getId())).withSelfRel());
        createdUser.add(linkTo(methodOn(UserController.class).updateUser(createdUser.getId(), null)).withRel("update"));
        createdUser.add(linkTo(methodOn(UserController.class).deleteUser(createdUser.getId())).withRel("delete"));
        createdUser.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));

        return ResponseEntity
                .created(linkTo(methodOn(UserController.class).getUserById(createdUser.getId())).toUri())
                .body(createdUser);
    }

    @Operation(summary = "Обновить пользователя",
            description = "Обновляет информацию о пользователе по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Неверные данные",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные пользователя", required = true)
            @Valid @RequestBody UserRequestDto requestDto) {

        UserResponseDto updatedUser = userService.updateUser(id, requestDto);

        // Добавляем HATEOAS ссылки
        updatedUser.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        updatedUser.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        updatedUser.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));

        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя по ID и отправляет событие в Kafka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id) {

        userService.deleteUser(id);

        // Возвращаем ссылку на список пользователей
        Link allUsersLink = linkTo(methodOn(UserController.class).getAllUsers())
                .withRel(IanaLinkRelations.COLLECTION);

        return ResponseEntity.noContent()
                .header("Link", allUsersLink.toString())
                .build();
    }
}