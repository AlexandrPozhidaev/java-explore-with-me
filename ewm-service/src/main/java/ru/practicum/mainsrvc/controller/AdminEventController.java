package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.EventFullDto;
import ru.practicum.mainsrvc.dto.StateActionDto;
import ru.practicum.mainsrvc.dto.UpdateEventRequestDto;
import ru.practicum.mainsrvc.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getAdminEvents(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (from < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }

        return ResponseEntity.ok(eventService.getAdminEventsList(from, size));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventAdmin(
            @PathVariable Long eventId,
            @RequestBody UpdateEventRequestDto dto) {
        return ResponseEntity.ok(eventService.updateEventByAdmin(eventId, dto));
    }

    @PatchMapping("/{eventId}/state")
    public ResponseEntity<EventFullDto> updateEventStateByAdmin(
            @PathVariable Long eventId,
            @RequestBody StateActionDto dto) {

        switch (dto.getStateAction()) {
            case PUBLISH_EVENT:
                return ResponseEntity.ok(eventService.publishEvent(eventId));
            case REJECT_EVENT:
                return ResponseEntity.ok(eventService.rejectEvent(eventId));
            default:
                throw new IllegalArgumentException(
                        "Неподдерживаемое админское действие: " + dto.getStateAction()
                );
        }
    }
}