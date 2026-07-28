package ru.practicum.mainsrvc.service;

import jakarta.persistence.EntityNotFoundException;
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
import ru.practicum.mainsrvc.entity.Category;
import ru.practicum.mainsrvc.entity.Event;
import ru.practicum.mainsrvc.entity.EventStatus;
import ru.practicum.mainsrvc.entity.User;
import ru.practicum.mainsrvc.exception.ConflictException;
import ru.practicum.mainsrvc.exception.ForbiddenException;
import ru.practicum.mainsrvc.exception.NotFoundException;
import ru.practicum.mainsrvc.repository.CategoryRepository;
import ru.practicum.mainsrvc.repository.EventRepository;
import ru.practicum.mainsrvc.repository.RequestRepository;
import ru.practicum.mainsrvc.repository.UserRepository;
import ru.practicum.statclient.StatClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    public List<EventShortDto> getPublicEvents(
            List<Long> categories,
            Boolean paid,
            String text,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size) {

        if (from < 0 || size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        String searchText = (text != null && !text.isBlank()) ? text.trim() : null;

        List<Long> categoriesList = (categories != null && !categories.isEmpty()) ? categories : null;

        Sort sort = Sort.by("eventDate").ascending();
        Pageable pageable = PageRequest.of(from / size, size, sort);

        Page<Event> pageResult;
        if (categoriesList == null) {
            pageResult = eventRepository.findPublishedWithoutCategories(
                    paid, searchText, rangeStart, rangeEnd, pageable);
        } else {
            pageResult = eventRepository.findPublishedWithCategories(
                    categoriesList, paid, searchText, rangeStart, rangeEnd, pageable);
        }

        List<Event> events = pageResult.getContent();

        Map<String, Long> hitsMap = Collections.emptyMap();
        if (!events.isEmpty()) {
            List<String> uris = events.stream()
                    .map(e -> "/events/" + e.getId())
                    .collect(Collectors.toList());

            try {
                hitsMap = getHitsMap(uris);
            } catch (Exception ex) {
                log.warn("Не удалось получить статистику просмотров, возвращаем 0 для views", ex);
            }
        }

        List<EventShortDto> result = new ArrayList<>(events.size());
        for (Event e : events) {
            EventShortDto dto = toEventShortDto(e, hitsMap);
            result.add(dto);
        }

        return result;
    }

    private Map<String, Long> getHitsMap(List<String> uris) {
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

    @Transactional(readOnly = true)
    public EventShortDto getEventShortById(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или не опубликовано"));

        String uri = "/events/" + event.getId();
        Map<String, Long> hitsMap = Collections.emptyMap();

        LocalDateTime start = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.now();

        try {
            List<ViewStatsDto> stats = statClient.getStats(start, end, Collections.singletonList(uri), false);
            if (!stats.isEmpty()) {
                hitsMap = Collections.singletonMap(stats.get(0).getUri(), stats.get(0).getHits());
            }
        } catch (Exception ex) {
            log.warn("Не удалось получить статистику просмотров для события id={}", eventId, ex);
        }

        return toEventShortDto(event, hitsMap);
    }

    @Transactional
    public EventFullDto createEvent(NewEventDto dto, Long initiatorId) {
        var category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        var initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        LocalDateTime eventDate = dto.getEventDate();
        LocalDateTime minDate = LocalDateTime.now().plusHours(2);
        if (eventDate.isBefore(minDate)) {
            throw new IllegalArgumentException(
                    "Дата события должна быть не ранее чем через 2 часа от текущего времени"
            );
        }

        String description = dto.getDescription();
        if (description == null || description.length() < 20) {
            throw new IllegalArgumentException(
                    "Описание события должно содержать не менее 20 символов"
            );
        }

        String annotation = dto.getAnnotation();
        if (annotation == null || annotation.length() < 20 || annotation.length() > 2000) {
            throw new IllegalArgumentException(
                    "Аннотация события должна содержать от 20 до 2000 символов"
            );
        }

        String title = dto.getTitle();
        if (title == null || title.length() < 3 || title.length() > 120) {
            throw new IllegalArgumentException(
                    "Заголовок события должен содержать от 3 до 120 символов"
            );
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());

        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);

        event.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

        event.setCategory(category);
        event.setInitiator(initiator);
        event.setState(EventStatus.PENDING);

        event = eventRepository.save(event);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventRequestDto dto, Long initiatorId) {
        Event event = eventRepository.findByIdAndInitiator(eventId, initiatorId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getState() == EventStatus.PUBLISHED) {
            throw new ConflictException("Нельзя редактировать опубликованное событие");
        }

        if (event.getState() == EventStatus.CANCELED) {
            throw new ConflictException("Нельзя редактировать отмененное событие");
        }

        String title = dto.getTitle();
        if (title != null && (title.length() < 3 || title.length() > 120)) {
            throw new ValidationException("Заголовок должен содержать от 3 до 120 символов");
        }

        String description = dto.getDescription();
        if (description != null && (description.length() < 20 || description.length() > 7000)) {
            throw new ValidationException("Описание должно содержать от 20 до 7000 символов");
        }

        String annotation = dto.getAnnotation();
        if (annotation != null && (annotation.length() < 20 || annotation.length() > 2000)) {
            throw new ValidationException("Аннотация должна содержать от 20 до 2000 символов");
        }

        Integer participantLimit = dto.getParticipantLimit();
        if (participantLimit != null && participantLimit < 0) {
            throw new ValidationException("participantLimit не может быть отрицательным");
        }

        LocalDateTime newEventDate = dto.getEventDate();
        if (newEventDate != null) {
            LocalDateTime now = LocalDateTime.now();

            if (newEventDate.isBefore(now)) {
                throw new ValidationException("Дата события не может быть в прошлом");
            }

            if (event.getState() == EventStatus.PENDING) {
                LocalDateTime minDate = now.plusHours(2);
                if (newEventDate.isBefore(minDate)) {
                    throw new ValidationException(
                            "Дата события должна быть не ранее чем через 2 часа от текущего времени"
                    );
                }
            }

            event.setEventDate(newEventDate);
        }

        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (dto.getPinned() != null) {
            event.setPinned(dto.getPinned());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getCategoryId() != null) {
            var category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        event = eventRepository.save(event);
        log.info("Событие id={} обновлено пользователем id={}", eventId, initiatorId);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequestDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!EventStatus.PENDING.equals(event.getState())) {
            throw new ConflictException("Событие должно находиться в состоянии PENDING для изменения администратором");
        }

        LocalDateTime newEventDate = dto.getEventDate();
        if (newEventDate != null) {
            LocalDateTime now = LocalDateTime.now();
            if (newEventDate.isBefore(now)) {
                throw new ValidationException("Дата события не может быть в прошлом");
            }

            if (event.getPublishedOn() != null) {
                LocalDateTime minEventDate = event.getPublishedOn().minusHours(1);
                if (newEventDate.isBefore(minEventDate)) {
                    throw new ValidationException(
                            "Дата события не может быть раньше чем за 1 час до даты публикации");
                }
            }

            event.setEventDate(newEventDate);
        }

        String title = dto.getTitle();
        if (title != null) {
            if (title.length() < 3 || title.length() > 120) {
                throw new ValidationException("Заголовок должен содержать от 3 до 120 символов");
            }
            event.setTitle(title);
        }

        String description = dto.getDescription();
        if (description != null) {
            if (description.length() < 20 || description.length() > 7000) {
                throw new ValidationException("Описание должно содержать от 20 до 7000 символов");
            }
            event.setDescription(description);
        }

        String annotation = dto.getAnnotation();
        if (annotation != null) {
            if (annotation.length() < 20 || annotation.length() > 2000) {
                throw new ValidationException("Аннотация должна содержать от 20 до 2000 символов");
            }
            event.setAnnotation(annotation);
        }

        Integer participantLimit = dto.getParticipantLimit();
        if (participantLimit != null) {
            if (participantLimit < 0) {
                throw new ValidationException("participantLimit не может быть отрицательным");
            }
            event.setParticipantLimit(participantLimit);
        }

        if (dto.getPinned() != null) {
            event.setPinned(dto.getPinned());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }

        if (dto.getCategoryId() != null) {
            var category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        event = eventRepository.save(event);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional
    public EventFullDto updateEventState(Long userId, Long eventId, StateActionDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь не является инициатором события");
        }

        if (event.getState() != EventStatus.PENDING) {
            throw new ConflictException(
                    "Можно изменить статус только для события в состоянии PENDING. Текущий статус: " + event.getState()
            );
        }

        switch (dto.getStateAction()) {
            case SEND_TO_REVIEW:
                log.debug("Событие id={} отправлено на модерацию", eventId);
                break;

            case CANCEL:
                event.setState(EventStatus.CANCELED);
                log.info("Событие id={} отменено пользователем id={}", eventId, userId);
                break;

            default:
                throw new IllegalArgumentException("Неизвестное действие: " + dto.getStateAction());
        }

        event = eventRepository.save(event);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventFullByIdForUser(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new EntityNotFoundException("Не хватает прав на просмотр страницы");
        }
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional(readOnly = false)
    public EventFullDto getEventFullByIdForPublicWithStats(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getState() != EventStatus.PUBLISHED) {
            throw new NotFoundException("Событие ещё не опубликовано");
        }

        String uri = "/events/" + event.getId();

        try {
            statClient.hit(uri, "ewm-service", "unknown-ip");
            log.debug("Отправлен просмотр для события {}", eventId);
        } catch (Exception ex) {
            log.warn("Не удалось отправить статистику просмотров для события id={}", eventId, ex);
        }

        long views = 0;
        try {
            List<ViewStatsDto> stats = statClient.getStats(
                    LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
                    LocalDateTime.now(),
                    Collections.singletonList(uri),
                    false
            );
            if (!stats.isEmpty()) {
                views = stats.get(0).getHits();
            }
        } catch (Exception ex) {
            log.warn("Не удалось получить статистику просмотров для события id={}", eventId, ex);
        }

        Map<String, Long> hitsMap = Collections.singletonMap(uri, views);

        return toEventFullDto(event, hitsMap);
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> getAdminEventsList(int from, int size) {
        if (from < 0 || size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }

        var pageRequest = PageRequest.of(from, size);
        var pageResult = eventRepository.findAll(pageRequest);

        return pageResult.getContent().stream()
                .map(e -> toEventFullDto(e, Collections.emptyMap()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> getAdminEventsWithFilters(
            List<EventStatus> states,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size,
            List<Long> users,
            List<Long> categories) {

        if (from < 0 || size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }

        PageRequest pageRequest = PageRequest.of(from, size);

        List<String> statesStrings = null;
        if (states != null && !states.isEmpty()) {
            statesStrings = states.stream()
                    .map(EventStatus::name)
                    .collect(Collectors.toList());
        }

        List<Long> usersList = null;
        if (users != null && !users.isEmpty()) {
            usersList = users;
        }

        List<Long> categoriesList = null;
        if (categories != null && !categories.isEmpty()) {
            categoriesList = categories;
        }

        Page<Event> page = eventRepository.findByAdminFilters(
                statesStrings,
                rangeStart,
                rangeEnd,
                usersList,
                categoriesList,
                pageRequest
        );

        return page.getContent().stream()
                .map(e -> toEventFullDto(e, Collections.emptyMap()))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getState() != EventStatus.PENDING) {
            throw new ConflictException(
                    "Нельзя опубликовать событие: текущий статус — " + event.getState() +
                            ". Публикация разрешена только из состояния PENDING."
            );
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minEventDate = now.plusHours(1);
        if (event.getEventDate().isBefore(minEventDate)) {
            throw new IllegalArgumentException(
                    "Дата события должна быть не ранее чем через 1 час от текущего времени"
            );
        }

        event.setState(EventStatus.PUBLISHED);
        event.setPublishedOn(now);
        event = eventRepository.save(event);

        log.info("Событие id={} успешно опубликовано", eventId);
        return toEventFullDto(event, Collections.emptyMap());
    }

    @Transactional
    public EventFullDto rejectEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getState() != EventStatus.PENDING) {
            throw new ConflictException(
                    "Нельзя отклонить событие: текущий статус — " + event.getState() +
                            ". Отклонение разрешено только из состояния PENDING."
            );
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

    @Transactional(readOnly = true)
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено: " + eventId));
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

        dto.setCategory(e.getCategory() != null ? toCategoryDto(e.getCategory()) : null);
        dto.setInitiator(e.getInitiator() != null ? toUserShortDto(e.getInitiator()) : null);

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
        dto.setState(e.getState());

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
