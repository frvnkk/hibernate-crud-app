package com.example.util;

import com.example.dto.UserResponseDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class HateoasUtils {

    private HateoasUtils() {}

    public static void addUserLinks(UserResponseDto user) {
        Long id = user.getId();

        user.add(linkTo(methodOn(com.example.controller.UserController.class)
                .getUserById(id)).withSelfRel());
        user.add(linkTo(methodOn(com.example.controller.UserController.class)
                .updateUser(id, null)).withRel("update"));
        user.add(linkTo(methodOn(com.example.controller.UserController.class)
                .deleteUser(id)).withRel("delete"));
    }

    public static Link getCollectionLink() {
        return linkTo(methodOn(com.example.controller.UserController.class)
                .getAllUsers()).withRel("users");
    }
}