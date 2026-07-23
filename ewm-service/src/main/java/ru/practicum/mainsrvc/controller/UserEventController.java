package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.EventShortDto;
import ru.practicum.mainsrvc.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserEventController {

    private final EventService eventService;

    public UserEventController(EventService eventService) {
        this.eventService = eventService;
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