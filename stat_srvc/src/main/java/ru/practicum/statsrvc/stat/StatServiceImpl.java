package ru.practicum.statsrvc.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatServiceImpl implements StatService {

    private static final Logger log = LoggerFactory.getLogger(StatServiceImpl.class);
    private final StatRepository repository;

    public StatServiceImpl(StatRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveHit(StatDto dto) {
        log.trace("Вызван saveHit: app={}, uri={}, ip={}",
                dto.getApp(), dto.getUri(), dto.getIp());
        Stat entity = new Stat();
        entity.setApp(dto.getApp());
        entity.setUri(dto.getUri());
        entity.setIp(dto.getIp());
        entity.setTimestamp(LocalDateTime.now());

        try {
            repository.save(entity);
            log.debug("Успешно: app={}, uri={}",
                    entity.getApp(), entity.getUri());
        } catch (Exception e) {
            log.error("Ошибка: app={}, uri={}, ip={}",
                    dto.getApp(), dto.getUri(), dto.getIp(), e);
            throw e;
        }
    }

    @Override
    public List<ViewStatsDto> getStats(List<String> uris, LocalDateTime start, LocalDateTime end, boolean unique) {
        log.trace("Вызван getStats: urisCount={}, start={}, end={}, unique={}",
                (uris != null ? uris.size() : 0), start, end, unique);

        if (start == null || end == null) {
            log.warn("getStats: Параметры start и end обязательны");
            throw new IllegalArgumentException("Параметры start и end обязательны");
        }

        List<String> filterUris = (uris == null || uris.isEmpty()) ? null : uris;

        try {
            List<ViewStatsDto> result = repository.findStatsByRange(start, end, filterUris);
            log.debug("Успешно, resultSize={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("Ошибка: urisCount={}, start={}, end={}, unique={}",
                    (uris != null ? uris.size() : 0), start, end, unique, e);
            throw e;
        }
    }
}
