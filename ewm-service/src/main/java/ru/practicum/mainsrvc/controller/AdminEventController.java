package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.EventFullDto;
import ru.practicum.mainsrvc.dto.StateActionDto;
import ru.practicum.mainsrvc.dto.UpdateEventRequestDto;
import ru.practicum.mainsrvc.entity.EventStatus;
import ru.practicum.mainsrvc.service.EventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getAdminEvents(
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> categories) {

        List<EventStatus> statusList = null;
        if (states != null && !states.isEmpty()) {
            statusList = states.stream()
                    .map(EventStatus::valueOf)
                    .collect(Collectors.toList());
        }

        List<EventFullDto> events = eventService.getAdminEventsWithFilters(
                statusList, rangeStart, rangeEnd, from, size, users, categories
        );

        return ResponseEntity.ok(events);
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