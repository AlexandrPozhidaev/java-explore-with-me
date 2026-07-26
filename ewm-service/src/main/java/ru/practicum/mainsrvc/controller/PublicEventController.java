package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.EventFullDto;
import ru.practicum.mainsrvc.dto.EventShortDto;
import ru.practicum.mainsrvc.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@Validated
public class PublicEventController {

    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getPublicEvents(
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean sortByDate) {

        List<EventShortDto> result = eventService.getPublicEvents(
                categories, paid, text, rangeStart, rangeEnd, from, size, sortByDate);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long id) {
        EventFullDto dto = eventService.getEventFullByIdForPublicWithStats(id);
        return ResponseEntity.ok(dto);
    }
}
