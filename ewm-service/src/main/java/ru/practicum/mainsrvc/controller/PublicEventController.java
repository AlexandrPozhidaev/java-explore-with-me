package ru.practicum.mainsrvc.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.EventFullDto;
import ru.practicum.mainsrvc.dto.EventShortDto;
import ru.practicum.mainsrvc.dto.NewEventDto;
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

    @PostMapping
    public ResponseEntity<EventShortDto> createEvent(@Valid @RequestBody NewEventDto dto) {
        EventFullDto full = eventService.createEvent(dto, dto.getUserId());

        EventShortDto shortDto = new EventShortDto();
        shortDto.setId(full.getId());
        shortDto.setTitle(full.getTitle());
        shortDto.setPinned(full.getPinned());
        shortDto.setPaid(full.getPaid());
        shortDto.setEventDate(full.getEventDate());
        shortDto.setViews(full.getViews());
        shortDto.setCategory(full.getCategory());
        shortDto.setInitiator(full.getInitiator());

        return ResponseEntity.status(201).body(shortDto);
    }
}
