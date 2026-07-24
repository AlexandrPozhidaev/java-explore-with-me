package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.mainsrvc.dto.EventShortDto;
import ru.practicum.mainsrvc.service.EventService;

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
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean sortByDate) {

        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' должен быть >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Параметр 'size' должен быть в диапазоне (0, 100]");
        }

        List<EventShortDto> events = eventService.getPublicEvents(categories, paid, text, from, size, sortByDate);
        return ResponseEntity.ok(events);
    }
}
