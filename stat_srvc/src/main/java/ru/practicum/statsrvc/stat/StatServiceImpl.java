package ru.practicum.statsrvc.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class StatServiceImpl implements StatService {

    private static final Logger log = LoggerFactory.getLogger(StatServiceImpl.class);
    private final StatRepository repository;

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$|^localhost$|^unknown-ip$|^127\\.0\\.0\\.1$|^172\\.18\\.0\\.[0-9]+$"
    );

    public StatServiceImpl(StatRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveHit(StatDto dto) {
        log.trace("Вызван saveHit: app={}, uri={}, ip={}",
                dto.getApp(), dto.getUri(), dto.getIp());

        if (dto.getTimestamp() == null) {
            throw new IllegalArgumentException("timestamp не может быть пустым");
        }

        String ip = dto.getIp();
        if (ip == null || !IP_PATTERN.matcher(ip).matches()) {
            log.warn("IP имеет неверный формат: {}, используем 'unknown'", ip);
            ip = "unknown";
        }

        if (dto.getApp() == null || dto.getApp().isBlank() || dto.getApp().length() > 255) {
            throw new IllegalArgumentException("app не может быть null, пустым или длиннее 255 символов");
        }
        if (dto.getUri() == null || dto.getUri().isBlank() || dto.getUri().length() > 1024) {
            throw new IllegalArgumentException("uri не может быть null, пустым или длиннее 1024 символов");
        }

        Stat entity = new Stat();
        entity.setApp(dto.getApp());
        entity.setUri(dto.getUri());
        entity.setIp(ip);
        entity.setTimestamp(dto.getTimestamp());

        repository.save(entity);
        log.debug("Успешно сохранен хит: app={}, uri={}, ip={}",
                entity.getApp(), entity.getUri(), entity.getIp());
    }

    @Override
    public List<ViewStatsDto> getStats(List<String> uris, LocalDateTime start, LocalDateTime end, boolean unique) {
        log.trace("Вызван getStats: urisCount={}, start={}, end={}, unique={}",
                (uris != null ? uris.size() : 0), start, end, unique);

        if (start == null || end == null) {
            throw new IllegalArgumentException("Параметры start и end обязательны");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Параметр start не может быть позже end");
        }

        List<String> filterUris = (uris == null || uris.isEmpty()) ? null : uris;

        List<ViewStatsDto> result;
        if (unique) {
            result = repository.findUniqueStats(filterUris, start, end);
        } else {
            result = repository.findAllStats(filterUris, start, end);
        }

        log.debug("Статистика получена, resultSize={}", result.size());
        return result;
    }
}