package ru.practicum.statclient;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/")
public class StatClientController {

    private final StatsClient statsClient;

    public StatClientController(StatsClient statsClient) {
        this.statsClient = statsClient;
    }

    @PostMapping("/hit")
    public ResponseEntity<StatDto> hit(@RequestBody StatDto statDto) {
        StatDto result = statsClient.hit(statDto.getUri(), statDto.getApp(), statDto.getIp());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(name = "start") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
            @RequestParam(name = "end") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        String startStr = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endStr = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        List<ViewStatsDto> result = statsClient.getStats(startStr, endStr, uris, unique);

        return ResponseEntity.ok(result);
    }
}
