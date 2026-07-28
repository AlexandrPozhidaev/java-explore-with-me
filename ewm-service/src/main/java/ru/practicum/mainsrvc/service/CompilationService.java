package ru.practicum.mainsrvc.service;

import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mainsrvc.dto.CompilationDto;
import ru.practicum.mainsrvc.dto.EventShortDto;
import ru.practicum.mainsrvc.dto.NewCompilationDto;
import ru.practicum.mainsrvc.dto.UpdateCompilationDto;
import ru.practicum.mainsrvc.entity.Compilation;
import ru.practicum.mainsrvc.entity.Event;
import ru.practicum.mainsrvc.exception.ConflictException;
import ru.practicum.mainsrvc.exception.NotFoundException;
import ru.practicum.mainsrvc.repository.CompilationRepository;
import ru.practicum.mainsrvc.repository.EventRepository;
import ru.practicum.statclient.StatClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompilationService {

    private static final Logger log = LoggerFactory.getLogger(CompilationService.class);

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatClient statClient;

    public CompilationService(CompilationRepository compilationRepository,
                              EventRepository eventRepository,
                              StatClient statClient) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.statClient = statClient;
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        if (compilationRepository.existsByTitle(dto.getTitle())) {
            throw new ConflictException("Подборка '" + dto.getTitle() + "' уже существует");
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new ValidationException("Заголовок подборки не может быть пустым");
        }
        if (dto.getTitle().length() < 3 || dto.getTitle().length() > 50) {
            throw new ValidationException("Заголовок должен содержать от 3 до 50 символов");
        }

        Compilation c = new Compilation();
        c.setTitle(dto.getTitle().trim());
        c.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        c.setPinned(dto.isPinned());

        List<Event> events = new ArrayList<>();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(dto.getEvents());
            if (events.size() != dto.getEvents().size()) {
                Set<Long> foundIds = events.stream().map(Event::getId).collect(Collectors.toSet());
                List<Long> notFound = dto.getEvents().stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                throw new NotFoundException("События не найдены: " + notFound);
            }
            c.getEvents().addAll(events);
        }

        c = compilationRepository.save(c);

        Map<String, Long> hitsMap = Collections.emptyMap();
        if (!events.isEmpty()) {
            List<String> uris = events.stream()
                    .map(e -> "/events/" + e.getId())
                    .collect(Collectors.toList());
            try {
                hitsMap = getStatsForUris(uris);
            } catch (Exception ex) {
                log.warn("Не удалось получить статистику просмотров", ex);
            }
        }

        return toCompilationDto(c, hitsMap);
    }

    @Transactional(readOnly = true)
    public List<CompilationDto> getPublicCompilations(Boolean pinned, int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Параметр 'size' должен быть больше 0 и не более 1000");
        }

        Sort sort = pinned != null
                ? Sort.by("pinned").descending().and(Sort.by("id").ascending())
                : Sort.by("id").ascending();

        Pageable pageable = PageRequest.of(from / size, size, sort);
        Page<Compilation> compsPage = compilationRepository.findAllOrByPinned(pinned, pageable);
        List<Compilation> comps = compsPage.getContent();

        Map<Long, Map<String, Long>> statsMap = collectStatsForCompilations(comps);

        return comps.stream()
                .map(c -> toCompilationDto(c, statsMap.getOrDefault(c.getId(), Collections.emptyMap())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long id) {
        Compilation c = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена: " + id));

        Map<String, Long> hitsMap = collectStatsForCompilation(c);
        return toCompilationDto(c, hitsMap);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto dto) {
        Compilation c = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена: " + compId));

        if (dto.getTitle() != null && !dto.getTitle().equals(c.getTitle())) {
            String newTitle = dto.getTitle().trim();
            if (newTitle.length() < 3 || newTitle.length() > 50) {
                throw new ValidationException("Заголовок должен содержать от 3 до 50 символов");
            }
            if (compilationRepository.existsByTitle(newTitle)) {
                throw new ConflictException("Подборка с таким заголовком уже существует");
            }
            c.setTitle(newTitle);
        }

        if (dto.getDescription() != null) {
            c.setDescription(dto.getDescription().trim());
        }

        if (dto.getPinned() != null) {
            c.setPinned(dto.getPinned());
        }

        c = compilationRepository.save(c);

        Map<String, Long> hitsMap = collectStatsForCompilation(c);
        return toCompilationDto(c, hitsMap);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка не найдена: " + compId);
        }
        compilationRepository.deleteById(compId);
    }

    private Map<String, Long> getStatsForUris(List<String> uris) {
        try {
            LocalDateTime start = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
            LocalDateTime end = LocalDateTime.now();
            List<ViewStatsDto> stats = statClient.getStats(start, end, uris, false);
            return stats.stream()
                    .collect(Collectors.toMap(
                            ViewStatsDto::getUri,
                            ViewStatsDto::getHits,
                            (v1, v2) -> v1
                    ));
        } catch (Exception e) {
            log.warn("Ошибка получения статистики: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, Map<String, Long>> collectStatsForCompilations(List<Compilation> comps) {
        Map<Long, Map<String, Long>> result = new HashMap<>();

        for (Compilation c : comps) {
            Map<String, Long> hitsMap = collectStatsForCompilation(c);
            result.put(c.getId(), hitsMap);
        }

        return result;
    }

    private Map<String, Long> collectStatsForCompilation(Compilation c) {
        if (c.getEvents() == null || c.getEvents().isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = c.getEvents().stream()
                .filter(Objects::nonNull)
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());

        if (uris.isEmpty()) {
            return Collections.emptyMap();
        }

        return getStatsForUris(uris);
    }

    private EventShortDto toEventShortDto(Event e, Map<String, Long> hitsMap) {
        EventShortDto dto = new EventShortDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setPinned(e.getPinned());
        dto.setPaid(e.getPaid());
        dto.setEventDate(e.getEventDate());

        String uri = "/events/" + e.getId();
        Long views = hitsMap != null ? hitsMap.getOrDefault(uri, 0L) : 0L;
        dto.setViews(views);

        return dto;
    }

    private CompilationDto toCompilationDto(Compilation c, Map<String, Long> hitsMap) {
        CompilationDto dto = new CompilationDto();
        dto.setId(c.getId());
        dto.setPinned(c.getPinned());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());

        List<EventShortDto> eventDtos = new ArrayList<>();
        for (Event e : c.getEvents()) {
            if (e != null) {
                eventDtos.add(toEventShortDto(e, hitsMap));
            }
        }
        dto.setEvents(eventDtos);

        return dto;
    }
}