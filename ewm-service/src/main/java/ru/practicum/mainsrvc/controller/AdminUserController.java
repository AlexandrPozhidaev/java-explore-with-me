package ru.practicum.mainsrvc.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.UserFullDto;
import ru.practicum.mainsrvc.dto.UserShortDto;
import ru.practicum.mainsrvc.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    private static final int MAX_PAGE_SIZE = 100000;
    private static final int MIN_PAGE_SIZE = 1;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserFullDto> createUser(@Valid @RequestBody UserFullDto dto) {
        UserFullDto created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<UserShortDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' должен быть >= 0");
        }
        if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Размер страницы (size) должен быть от %d до %d",
                            MIN_PAGE_SIZE, MAX_PAGE_SIZE)
            );
        }

        List<UserShortDto> users;
        if (ids != null && !ids.isEmpty()) {
            users = userService.getUsersByIds(ids);
        } else {
            users = userService.getAllUsers(from, size);
        }

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserShortDto> getUserById(@PathVariable Long id) {
        UserShortDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUsers(@RequestParam List<Long> ids) {
        userService.deleteUsers(ids);
        return ResponseEntity.noContent().build();
    }
}