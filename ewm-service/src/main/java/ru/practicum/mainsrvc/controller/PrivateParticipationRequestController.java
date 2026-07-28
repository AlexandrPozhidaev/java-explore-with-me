package ru.practicum.mainsrvc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.ParticipationRequestDto;
import ru.practicum.mainsrvc.dto.ParticipationRequestStatusDto;
import ru.practicum.mainsrvc.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class PrivateParticipationRequestController {

    private final ParticipationRequestService requestService;

    public PrivateParticipationRequestController(ParticipationRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/{userId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' должен быть >= 0");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Параметр 'size' должен быть в диапазоне (0, 1000]");
        }

        var page = requestService.getRequestsByUser(userId, from, size);
        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping("/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {

        ParticipationRequestDto result = requestService.createRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {

        ParticipationRequestDto result = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<ParticipationRequestDto> approveOrRejectRequest(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody ParticipationRequestStatusDto dto) {

        if (dto == null || dto.getRequestId() == null) {
            throw new IllegalArgumentException("requestId не может быть null");
        }

        ParticipationRequestDto result = requestService.approveOrReject(
                dto.getRequestId(), userId, dto.getStatus());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsForEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' должен быть >= 0");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Параметр 'size' должен быть в диапазоне (0, 1000]");
        }

        var page = requestService.getRequestsByUserAndEvent(userId, eventId, from, size);
        return ResponseEntity.ok(page.getContent());
    }
}