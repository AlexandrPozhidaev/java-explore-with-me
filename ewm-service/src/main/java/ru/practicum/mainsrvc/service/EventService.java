package ru.practicum.mainsrvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mainsrvc.dto.*;
import ru.practicum.mainsrvc.entity.*;
import ru.practicum.mainsrvc.exception.EntityNotFoundException;
import ru.practicum.mainsrvc.repository.*;
import ru.practicum.statclient.StatClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StatClient statClient;

    public EventService(EventRepository eventRepository,
                        RequestRepository requestRepository,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository,
                        StatClient statClient) {
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.statClient = statClient;
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(List<Long> categories, Boolean paid,
                                               String text, int from, int size,
                                               boolean sortByDate) {
        if (from < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Некорректные параметры пагинации: from >= 0, 0 < size <= 100");
        }

        int page = from / size;
        Sort sort = sortByDate
                ? Sort.by("eventDate").ascending()
                : Sort.unsorted();

        List<Long> categoryIdsFilter = (categories == null || categories.isEmpty())
                ? null
                : categories;

        var pageResult = eventRepository.findPublished(categoryIdsFilter, paid, text, PageRequest.of(page, size, sort));
        List<Event> events = pageResult.getContent();

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());

        Map<String, Long> hitsMap;
        if (!uris.isEmpty()) {
            LocalDateTime start = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
            LocalDateTime end = LocalDateTime.now();
            List<ViewStatsDto> stats = statClient.getStats(start, end, uris, false);

            hitsMap = stats.stream()
                    .collect(Collectors.toMap(
                            ViewStatsDto::getUri,
                            ViewStatsDto::getHits,
                            (v1, v2) -> v1
                    ));
        } else {
            hitsMap = Collections.emptyMap();
        }

        return events.stream()
                .map(e -> toEventShortDto(e, hitsMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventShortDto getEventShortById(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено или не опубликовано"));

        String uri = "/events/" + event.getId();
        Map<String, Long> hitsMap = Collections.emptyMap();

        LocalDateTime start = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.now();
        List<ViewStatsDto> stats = statClient.getStats(start, end, Collections.singletonList(uri), false);
        if (!stats.isEmpty()) {
            hitsMap = Collections.singletonMap(stats.get(0).getUri(), stats.get(0).getHits());
        }

        return toEventShortDto(event, hitsMap);
    }

    @Transactional
    public EventFullDto createEvent(NewEventDto dto, Long initiatorId) {
        var category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена"));

        var initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setPinned(dto.getPinned());
        event.setPaid(dto.getPaid());
        event.setRequestModeration(dto.getRequestModeration());
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setState(EventStatus.PENDING);

        event = eventRepository.save(event);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventRequestDto dto, Long initiatorId) {
        Event event = eventRepository.findByIdAndInitiator(eventId, initiatorId)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено"));

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getPinned() != null) event.setPinned(dto.getPinned());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getRequestModeration() != null)
            event.setRequestModeration(dto.getRequestModeration());
        if (dto.getCategoryId() != null) {
            var category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        event = eventRepository.save(event);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventFullByIdForUser(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new EntityNotFoundException("Не хватает прав на просмотр страницы");
        }
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional
    public EventFullDto publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено"));

        if (event.getState() == EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Событие уже опубликовано");
        }

        event.setState(EventStatus.PUBLISHED);
        event = eventRepository.save(event);

        log.info("Событие id={} успешно опубликовано", eventId);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional
    public EventFullDto canceledEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено"));

        if (event.getState() == EventStatus.CANCELED) {
            log.warn("Попытка отклонить уже отклоненное событие id={}", eventId);
        }

        event.setState(EventStatus.CANCELED);
        event = eventRepository.save(event);

        log.info("Событие id={} успешно отклонено", eventId);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        if (from < 0 || size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Некорректные параметры пагинации: from >= 0, 0 < size <= 1000");
        }

        var pageRequest = PageRequest.of(from, size, Sort.unsorted());
        var eventsPage = eventRepository.findAllByInitiatorId(userId, pageRequest);

        List<Event> events = eventsPage.getContent();

        return events.stream()
                .map(this::toEventShortDto)
                .collect(Collectors.toList());
    }

    private EventShortDto toEventShortDto(Event e, Map<String, Long> hitsMap) {
        EventShortDto dto = new EventShortDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setPinned(e.getPinned());
        dto.setPaid(e.getPaid());
        dto.setEventDate(e.getEventDate());

        String uri = "/events/" + e.getId();
        dto.setViews(hitsMap.getOrDefault(uri, 0L));

        dto.setCategory(toCategoryDto(e.getCategory()));
        dto.setInitiator(toUserShortDto(e.getInitiator()));
        return dto;
    }

    private EventFullDto toEventFullDto(Event e, Map<String, Long> hitsMap) {
        EventFullDto dto = new EventFullDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setAnnotation(e.getAnnotation());
        dto.setDescription(e.getDescription());
        dto.setEventDate(e.getEventDate());
        dto.setParticipantLimit(e.getParticipantLimit());
        dto.setPinned(e.getPinned());
        dto.setPaid(e.getPaid());
        dto.setRequestModeration(e.getRequestModeration());

        String uri = "/events/" + e.getId();
        dto.setViews(hitsMap.getOrDefault(uri, 0L));

        dto.setCategory(toCategoryDto(e.getCategory()));
        dto.setInitiator(toUserShortDto(e.getInitiator()));
        dto.setConfirmedRequests(requestRepository.countConfirmedByEventId(e.getId()));

        return dto;
    }

    private EventShortDto toEventShortDto(Event e) {
        return toEventShortDto(e, Collections.emptyMap());
    }

    private EventFullDto toEventFullDto(Event e) {
        return toEventFullDto(e, Collections.emptyMap());
    }

    private CategoryDto toCategoryDto(Category c) {
        if (c == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        return dto;
    }

    private UserShortDto toUserShortDto(User u) {
        if (u == null) return null;
        UserShortDto dto = new UserShortDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        return dto;
    }
}
