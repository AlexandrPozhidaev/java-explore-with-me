package ru.practicum.statclient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/")
public class StatClientController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatClient statClient;

    public StatClientController(StatClient statClient) {
        this.statClient = statClient;
    }

    @PostMapping("/hit")
    public ResponseEntity<StatDto> hit(@RequestBody StatDto statDto) {
        StatDto result = statClient.hit(statDto.getUri(), statDto.getApp(), statDto.getIp());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "start") String startStr,
            @RequestParam(value = "end") String endStr,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        if (startStr == null || endStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметры start и end обязательны");
        }

        String startDecoded;
        String endDecoded;

        try {
            startDecoded = URLDecoder.decode(startStr, StandardCharsets.UTF_8);
            endDecoded = URLDecoder.decode(endStr, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка декодирования параметров start/end", e);
        }

        LocalDateTime start;
        LocalDateTime end;

        try {
            start = LocalDateTime.parse(startDecoded, FORMATTER);
            end = LocalDateTime.parse(endDecoded, FORMATTER);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Параметры start/end должны быть в формате 'yyyy-MM-dd HH:mm:ss'. " +
                            "Получено: start='" + startDecoded + "', end='" + endDecoded + "'",
                    e
            );
        }

        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр start не может быть позже end");
        }

        List<ViewStatsDto> result = statClient.getStats(start, end, uris, unique);
        return ResponseEntity.ok(result);
    }
}
