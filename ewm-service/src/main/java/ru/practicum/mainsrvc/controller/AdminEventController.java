package ru.practicum.mainsrvc.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.EventFullDto;
import ru.practicum.mainsrvc.dto.StateActionDto;
import ru.practicum.mainsrvc.dto.UpdateEventRequestDto;
import ru.practicum.mainsrvc.entity.EventStatus;
import ru.practicum.mainsrvc.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private static final Logger log = LoggerFactory.getLogger(AdminEventController.class);

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getAdminEvents(
            @RequestParam(required = false) List<EventStatus> states,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> categories) {

        if (from < 0) {
            throw new IllegalArgumentException("from не может быть отрицательным");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("size должен быть от 1 до 1000");
        }

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new IllegalArgumentException("rangeEnd не может быть раньше rangeStart");
        }

        log.debug("Admin events request: states={}, users={}, categories={}", states, users, categories);

        List<EventFullDto> events = eventService.getAdminEventsWithFilters(
                states, rangeStart, rangeEnd, from, size, users, categories
        );

        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequestDto dto) {
        return ResponseEntity.ok(eventService.updateEventByAdmin(eventId, dto));
    }

    @PatchMapping("/{eventId}/state")
    public ResponseEntity<EventFullDto> updateEventStateByAdmin(
            @PathVariable Long eventId,
            @RequestBody StateActionDto dto) {

        if (dto == null || dto.getStateAction() == null) {
            throw new IllegalArgumentException("stateAction не может быть null");
        }

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