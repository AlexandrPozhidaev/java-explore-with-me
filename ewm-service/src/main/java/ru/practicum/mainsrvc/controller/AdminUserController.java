package ru.practicum.mainsrvc.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.UserFullDto;
import ru.practicum.mainsrvc.dto.UserShortDto;
import ru.practicum.mainsrvc.service.UserService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

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
            @RequestParam(required = false) Long ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (ids != null) {
            UserShortDto user = userService.getUserByIdOrNull(ids);
            if (user == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(Collections.singletonList(user));
        } else {
            List<UserShortDto> users = userService.getAllUsers(from, size);
            return ResponseEntity.ok(users);
        }
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<UserShortDto> activateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.activateUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
