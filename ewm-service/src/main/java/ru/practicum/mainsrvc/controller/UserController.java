package ru.practicum.mainsrvc.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.*;
import ru.practicum.mainsrvc.service.EventService;
import ru.practicum.mainsrvc.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final ParticipationRequestService participationRequestService;
    private final EventService eventService;

    public UserController(
            ParticipationRequestService participationRequestService,
            EventService eventService) {
        this.participationRequestService = participationRequestService;
        this.eventService = eventService;
    }

    @GetMapping("/{userId}/requests")
    public ResponseEntity<Page<ParticipationRequestDto>> getUserRequests(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        validatePaginationParams(from, size);

        Page<ParticipationRequestDto> page = participationRequestService.getRequestsByUser(userId, from, size);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {

        ParticipationRequestDto result = participationRequestService.createRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {

        ParticipationRequestDto result = participationRequestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<Page<ParticipationRequestDto>> getRequestsForUserAndEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        validatePaginationParams(from, size);

        Page<ParticipationRequestDto> page = participationRequestService.getRequestsByUserAndEvent(
                userId, eventId, from, size);
        return ResponseEntity.ok(page);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<ParticipationRequestDto> approveOrRejectRequest(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody ParticipationRequestStatusDto dto) {

        if (dto == null || dto.getRequestId() == null) {
            throw new IllegalArgumentException("requestId не может быть null");
        }

        ParticipationRequestDto result = participationRequestService.approveOrReject(
                dto.getRequestId(), userId, dto.getStatus());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/events")
    public ResponseEntity<EventFullDto> createEventForUser(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto dto) {

        EventFullDto full = eventService.createEvent(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(full);
    }

    @GetMapping("/{userId}/events")
    public ResponseEntity<List<EventShortDto>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        validatePaginationParams(from, size);

        List<EventShortDto> events = eventService.getUserEvents(userId, from, size);
        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEventUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody UpdateEventRequestDto dto) {

        EventFullDto result = eventService.updateEvent(eventId, dto, userId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{userId}/events/{eventId}/state")
    public ResponseEntity<EventFullDto> updateEventState(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody StateActionDto stateActionDto) {

        EventFullDto result = eventService.updateEventState(userId, eventId, stateActionDto);
        return ResponseEntity.ok(result);
    }

    private void validatePaginationParams(int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' должен быть >= 0");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Параметр 'size' должен быть в диапазоне (0, 1000]");
        }
    }
}