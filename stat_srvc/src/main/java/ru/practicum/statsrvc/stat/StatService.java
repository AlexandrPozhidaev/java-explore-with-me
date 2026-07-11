package ru.practicum.statsrvc.stat;

import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    void saveHit(StatDto hit);
    List<ViewStatsDto> getStats(List<String> uris, LocalDateTime start, LocalDateTime end, boolean unique);
}
