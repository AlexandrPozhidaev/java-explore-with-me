package ru.practicum.mainsrvc.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.CreateRequestDto;
import ru.practicum.mainsrvc.dto.ParticipationRequestDto;
import ru.practicum.mainsrvc.service.ParticipationRequestService;

@RestController
@RequestMapping("/private/requests")
public class PrivateParticipationRequestController {

    private final ParticipationRequestService requestService;

    public PrivateParticipationRequestController(ParticipationRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/{userId}/{eventId}")
    public ResponseEntity<ParticipationRequestDto> createRequest(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody CreateRequestDto dto) {
        return ResponseEntity.status(201).body(requestService.createRequest(userId, eventId, dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ParticipationRequestDto>> getRequestsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' должен быть >= 0");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Параметр 'size' должен быть в диапазоне (0, 1000]");
        }

        Page<ParticipationRequestDto> page = requestService.getRequestsByUser(userId, from, size);
        return ResponseEntity.ok(page);
    }

    @PatchMapping("/approve/{requestId}")
    public ResponseEntity<ParticipationRequestDto> approveRequest(
            @PathVariable Long requestId,
            @RequestAttribute(name = "initiatorId") Long initiatorId) {
        return ResponseEntity.ok(requestService.approveRequestByInitiator(requestId, initiatorId));
    }
}
