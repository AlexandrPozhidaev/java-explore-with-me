package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.EventFullDto;
import ru.practicum.mainsrvc.dto.UpdateEventRequestDto;
import ru.practicum.mainsrvc.service.EventService;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventAdmin(
            @PathVariable Long eventId,
            @RequestBody UpdateEventRequestDto dto) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, dto, null));
    }

    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<EventFullDto> publishEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.publishEvent(eventId));
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventFullDto> cancelEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.canceledEvent(eventId));
    }
}
