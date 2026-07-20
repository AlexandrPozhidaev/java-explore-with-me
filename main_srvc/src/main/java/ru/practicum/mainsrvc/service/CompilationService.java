package ru.practicum.mainsrvc.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mainsrvc.dto.*;
import ru.practicum.mainsrvc.entity.*;
import ru.practicum.mainsrvc.exception.EntityNotFoundException;
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
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("from must be >= 0 and size > 0");
        }

        int page = from / size;
        Sort sort = pinned != null
                ? Sort.by("pinned").descending().and(Sort.by("id").ascending())
                : Sort.by("id").ascending();

        Page<Compilation> compsPage = compilationRepository.findAll(PageRequest.of(page, size, sort));
        List<Compilation> comps = compsPage.getContent();

        StatsData statsData = collectStatsForCompilations(comps);

        return comps.stream()
                .map(c -> toCompilationDto(c, statsData.eventMap, statsData.hitsMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long id) {
        Compilation c = compilationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compilation not found: " + id));

        StatsData statsData = collectStatsForCompilation(c);

        return toCompilationDto(c, statsData.eventMap, statsData.hitsMap);
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        if (compilationRepository.existsByTitle(dto.getTitle())) {
            throw new IllegalArgumentException("Подборка '" + dto.getTitle() + "' уже существует");
        }
        Compilation c = new Compilation(dto.getTitle(), dto.getDescription(), dto.isPinned());
        c = compilationRepository.save(c);
        return toCompilationDto(c, Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto dto) {
        Compilation c = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Подборка не найдена: " + compId));

        if (dto.getTitle() != null && !dto.getTitle().equals(c.getTitle())) {
            if (compilationRepository.existsByTitle(dto.getTitle())) {
                throw new IllegalArgumentException("Подборка '" + dto.getTitle() + "' уже существует");
            }
            c.setTitle(dto.getTitle());
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
            throw new EntityNotFoundException("Подборка не найдена: " + compId);
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
            for (CompilationEvent ce : c.getEvents()) {
                Event e = ce.getEvent();
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

        List<String> uris = eventMap.keySet().stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        List<ViewStatsDto> stats = Collections.emptyList();
        if (!uris.isEmpty()) {
            LocalDateTime start = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
            LocalDateTime end = LocalDateTime.now();
            stats = statClient.getStats(start, end, uris, false);
        }

        Map<String, Long> hitsMap = stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits,
                        (v1, v2) -> v1
                ));

        return new StatsData(eventMap, hitsMap);
    }

    private StatsData collectStatsForCompilation(Compilation c) {
        Set<Long> eventIds = new HashSet<>();
        for (CompilationEvent ce : c.getEvents()) {
            Event e = ce.getEvent();
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

        List<String> uris = eventMap.keySet().stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        List<ViewStatsDto> stats = Collections.emptyList();
        if (!uris.isEmpty()) {
            LocalDateTime start = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
            LocalDateTime end = LocalDateTime.now();
            stats = statClient.getStats(start, end, uris, false);
        }

        Map<String, Long> hitsMap = stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits,
                        (v1, v2) -> v1
                ));

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
        for (CompilationEvent ce : c.getEvents()) {
            Event e = ce.getEvent();
            if (e == null) continue;
            Event mapped = eventMap.get(e.getId());
            if (mapped == null) continue;

            eventsList.add(toEventShortDto(mapped, hitsMap));
        }
        dto.setEvents(eventsList);
        return dto;
    }
}
