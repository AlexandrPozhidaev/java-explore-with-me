package ru.practicum.statsrvc.stat;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        try {
            statService.saveHit(dto);
            log.info("Запись успешно внесена: app={}, uri={}", dto.getApp(), dto.getUri());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Ошибка записи: app={}, uri={}", dto.getApp(), dto.getUri(), e);
            throw e;
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "start") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
            @RequestParam(value = "end") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        log.trace("Вызов getStats: urisCount={}, start={}, end={}, unique={}",
                (uris != null ? uris.size() : 0), start, end, unique);

        if (start == null || end == null) {
            log.warn("getStats: Параметры start и end обязательны");
            throw new IllegalArgumentException("Параметры start и end обязательны");
        }

        List<ViewStatsDto> stats = statService.getStats(uris, start, end, unique);
        return ResponseEntity.ok(stats);
    }
}