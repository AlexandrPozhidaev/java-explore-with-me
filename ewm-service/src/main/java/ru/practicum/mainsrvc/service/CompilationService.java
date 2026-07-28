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
import ru.practicum.mainsrvc.dto.*;
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

    @Transactional(readOnly = true)
    public List<CompilationDto> getPublicCompilations(Boolean pinned, int from, int size) {
        if (from < 0) {
            throw new ConflictException("Параметр from не может быть отрицательным");
        }
        if (size <= 0 || size > 1000) {
            throw new ConflictException("Параметр size должен быть больше 0 и не более 1000");
        }

        Sort sort = pinned != null
                ? Sort.by("pinned").descending().and(Sort.by("id").ascending())
                : Sort.by("id").ascending();

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Compilation> compsPage = compilationRepository.findAllOrByPinned(pinned, pageable);
        List<Compilation> comps = compsPage.getContent();

        int skip = from % size;
        if (skip > 0 && !comps.isEmpty()) {
            int startIndex = Math.min(skip, comps.size());
            comps = comps.subList(startIndex, comps.size());
        }

        StatsData statsData = collectStatsForCompilations(comps);

        return comps.stream()
                .map(c -> toCompilationDto(c, statsData.eventMap, statsData.hitsMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long id) {
        Compilation c = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + id));

        StatsData statsData = collectStatsForCompilation(c);

        return toCompilationDto(c, statsData.eventMap, statsData.hitsMap);
    }

    @Transactional
    public CompilationCreatedDto createCompilation(NewCompilationDto dto) {
        if (compilationRepository.existsByTitle(dto.getTitle())) {
            throw new ConflictException("Подборка '" + dto.getTitle() + "' уже существует");
        }

        Compilation c = new Compilation();
        c.setTitle(dto.getTitle());
        c.setDescription(dto.getDescription());
        c.setPinned(dto.isPinned());

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            if (events.size() != dto.getEvents().size()) {
                throw new NotFoundException("Одно или несколько событий не найдены");
            }
            c.getEvents().addAll(events);
        }

        c = compilationRepository.save(c);

        List<Long> eventIds = dto.getEvents() == null ? List.of() : dto.getEvents();

        return new CompilationCreatedDto(
                c.getId(),
                c.getPinned(),
                c.getTitle(),
                c.getDescription(),
                eventIds
        );
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto dto) {
        Compilation c = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена: " + compId));

        if (dto.getTitle() != null && !dto.getTitle().equals(c.getTitle())) {
            String newTitle = dto.getTitle();

            if (newTitle.length() < 3 || newTitle.length() > 50) {
                throw new ValidationException("Заголовок должен содержать от 3 до 50 символов");
            }

            if (compilationRepository.existsByTitle(newTitle)) {
                throw new ConflictException("Подборка с таким заголовком уже существует");
            }

            c.setTitle(newTitle);
        }

        if (dto.getDescription() != null) {
            c.setDescription(dto.getDescription());
        }

        if (dto.getPinned() != null) {
            c.setPinned(dto.getPinned());
        }

        c = compilationRepository.save(c);
        return toCompilationDto(c, Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка не найдена: " + compId);
        }
        compilationRepository.deleteById(compId);
    }

    private static class StatsData {
        Map<Long, Event> eventMap;
        Map<String, Long> hitsMap;

        StatsData(Map<Long, Event> eventMap, Map<String, Long> hitsMap) {
            this.eventMap = eventMap;
            this.hitsMap = hitsMap;
        }
    }

    private StatsData collectStatsForCompilations(List<Compilation> comps) {
        Set<Long> eventIds = new HashSet<>();
        for (Compilation c : comps) {
            for (Event e : c.getEvents()) {
                if (e != null) {
                    eventIds.add(e.getId());
                }
            }
        }

        Map<Long, Event> eventMap = new HashMap<>();
        if (!eventIds.isEmpty()) {
            List<Event> events = eventRepository.findAllById(eventIds);
            for (Event e : events) {
                eventMap.put(e.getId(), e);
            }
        }

        Map<String, Long> hitsMap = new HashMap<>();

        if (!eventMap.isEmpty()) {
            List<String> uris = eventMap.keySet().stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.toList());

            try {
                LocalDateTime start = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
                LocalDateTime end = LocalDateTime.now();
                List<ViewStatsDto> stats = statClient.getStats(start, end, uris, false);
                hitsMap = stats.stream()
                        .collect(Collectors.toMap(
                                ViewStatsDto::getUri,
                                ViewStatsDto::getHits,
                                (v1, v2) -> v1
                        ));
            } catch (Exception e) {
                log.warn("Не удалось получить статистику просмотров, возвращаем пустую карту", e);
            }
        }

        return new StatsData(eventMap, hitsMap);
    }

    private StatsData collectStatsForCompilation(Compilation c) {
        Set<Long> eventIds = new HashSet<>();
        for (Event e : c.getEvents()) {
            if (e != null) {
                eventIds.add(e.getId());
            }
        }

        Map<Long, Event> eventMap = new HashMap<>();
        if (!eventIds.isEmpty()) {
            List<Event> events = eventRepository.findAllById(eventIds);
            for (Event e : events) {
                eventMap.put(e.getId(), e);
            }
        }

        Map<String, Long> hitsMap = new HashMap<>();

        if (!eventMap.isEmpty()) {
            List<String> uris = eventMap.keySet().stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.toList());

            try {
                LocalDateTime start = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
                LocalDateTime end = LocalDateTime.now();
                List<ViewStatsDto> stats = statClient.getStats(start, end, uris, false);
                hitsMap = stats.stream()
                        .collect(Collectors.toMap(
                                ViewStatsDto::getUri,
                                ViewStatsDto::getHits,
                                (v1, v2) -> v1
                        ));
            } catch (Exception e) {
                log.warn("Не удалось получить статистику просмотров, возвращаем пустую карту", e);
            }
        }

        return new StatsData(eventMap, hitsMap);
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

    private CompilationDto toCompilationDto(Compilation c,
                                            Map<Long, Event> eventMap,
                                            Map<String, Long> hitsMap) {
        CompilationDto dto = new CompilationDto();
        dto.setId(c.getId());
        dto.setPinned(c.getPinned());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());

        List<EventShortDto> eventsList = new ArrayList<>();
        for (Event e : c.getEvents()) {
            if (e == null) continue;
            Event mapped = eventMap.get(e.getId());
            if (mapped == null) continue;

            eventsList.add(toEventShortDto(mapped, hitsMap));
        }
        dto.setEvents(eventsList);
        return dto;
    }
}