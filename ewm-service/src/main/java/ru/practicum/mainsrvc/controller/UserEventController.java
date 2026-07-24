package ru.practicum.mainsrvc.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.EventFullDto;
import ru.practicum.mainsrvc.dto.EventShortDto;
import ru.practicum.mainsrvc.dto.NewEventDto;
import ru.practicum.mainsrvc.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserEventController {

    private final EventService eventService;

    public UserEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/{userId}/events")
    public ResponseEntity<EventFullDto> createEventForUser(
            @PathVariable Long id,
            @Valid @RequestBody NewEventDto dto) {

        EventFullDto full = eventService.createEvent(dto, id);
        return ResponseEntity.status(201).body(full);
    }

    @GetMapping("/{userId}/events")
    public ResponseEntity<List<EventShortDto>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        List<EventShortDto> events = eventService.getUserEvents(userId, from, size);
        return ResponseEntity.ok(events);
    }
}