package ru.practicum.statsrvc.stat;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/")
public class StatController {

    private static final Logger log = LoggerFactory.getLogger(StatController.class);
    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @PostMapping("/hit")
    public ResponseEntity<Void> createHit(@Valid @RequestBody StatDto dto) {
        log.debug("Получен POST-запрос /hit: app={}, uri={}, ip={}",
                dto.getApp(), dto.getUri(), dto.getIp());
        statService.saveHit(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        if (start == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр start обязателен");
        }
        if (end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр end обязателен");
        }

        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр start не может быть позже end");
        }

        List<ViewStatsDto> stats = statService.getStats(uris, start, end, unique);
        return ResponseEntity.ok(stats);
    }
}