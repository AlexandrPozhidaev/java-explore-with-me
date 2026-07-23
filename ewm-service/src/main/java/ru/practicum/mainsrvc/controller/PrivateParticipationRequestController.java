package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.CreateRequestDto;
import ru.practicum.mainsrvc.dto.ParticipationRequestDto;
import ru.practicum.mainsrvc.service.ParticipationRequestService;

import java.util.List;

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
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(requestService.getRequestsByUser(userId));
    }

    @PatchMapping("/approve/{requestId}")
    public ResponseEntity<ParticipationRequestDto> approveRequest(
            @PathVariable Long requestId,
            @RequestAttribute(name = "initiatorId") Long initiatorId) {
        return ResponseEntity.ok(requestService.approveRequestByInitiator(requestId, initiatorId));
    }
}
