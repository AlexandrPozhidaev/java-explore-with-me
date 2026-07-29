package ru.practicum.statsrvc.stat;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/")
public class StatController {

    private static final Logger log = LoggerFactory.getLogger(StatController.class);
    private final StatService statService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        try {
            String startStr = start
                    .replace("%20", " ")
                    .replace("+", " ")
                    .trim();

            String endStr = end
                    .replace("%20", " ")
                    .replace("+", " ")
                    .trim();

            if (!startStr.contains(":")) {
                startStr += " 00:00:00";
            }
            if (!endStr.contains(":")) {
                endStr += " 23:59:59";
            }

            LocalDateTime startDate = LocalDateTime.parse(startStr, FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(endStr, FORMATTER);

            if (startDate.isAfter(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Параметр start не может быть позже end");
            }

            List<ViewStatsDto> stats = statService.getStats(uris, startDate, endDate, unique);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Ошибка парсинга дат: start={}, end={}", start, end, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Неверный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss, получено: start=" + start + ", end=" + end);
        }
    }
}