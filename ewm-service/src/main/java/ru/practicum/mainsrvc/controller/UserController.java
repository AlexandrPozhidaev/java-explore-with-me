package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.CreateRequestDto;
import ru.practicum.mainsrvc.dto.ParticipationRequestDto;
import ru.practicum.mainsrvc.service.ParticipationRequestService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final ParticipationRequestService participationRequestService;

    public UserController(ParticipationRequestService participationRequestService) {
        this.participationRequestService = participationRequestService;
    }

    @PostMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<ParticipationRequestDto> createRequest(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody CreateRequestDto dto) {

        ParticipationRequestDto result = participationRequestService.createRequest(
                userId,
                eventId,
                dto
        );

        return ResponseEntity.status(201).body(result);
    }
}
