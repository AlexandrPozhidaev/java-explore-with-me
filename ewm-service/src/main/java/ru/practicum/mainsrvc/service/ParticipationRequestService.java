package ru.practicum.mainsrvc.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainsrvc.dto.ParticipationRequestDto;
import ru.practicum.mainsrvc.entity.*;
import ru.practicum.mainsrvc.exception.ConflictException;
import ru.practicum.mainsrvc.exception.NotFoundException;
import ru.practicum.mainsrvc.repository.EventRepository;
import ru.practicum.mainsrvc.repository.RequestRepository;
import ru.practicum.mainsrvc.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParticipationRequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ParticipationRequestService(RequestRepository requestRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId, String comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getInitiator() != null && event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать запрос на участие в собственном событии");
        }

        if (!event.getState().equals(EventStatus.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в событии, которое не опубликовано");
        }

        boolean hasActiveRequest = requestRepository.existsByRequesterIdAndEventIdAndStatusNot(
                userId, eventId, RequestStatus.CANCELLED);
        if (hasActiveRequest) {
            throw new ConflictException("Запрос на участие уже существует");
        }

        long confirmedCount = requestRepository.countConfirmedByEventId(eventId);
        if (confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников для этого события");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequesterId(userId);
        request.setComment(comment);

        if (!event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        request = requestRepository.save(request);
        return toDto(request);
    }

    public Page<ParticipationRequestDto> getRequestsByUser(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        Page<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId, pageable);
        return requests.map(this::toDto);
    }

    public List<ParticipationRequestDto> getRequestsForEvent(Long eventId) {
        var requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ParticipationRequestDto approveRequestByInitiator(Long requestId, Long initiatorId) {
        ParticipationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        Event event = req.getEvent();
        Long eventInitiatorId = event.getInitiator() != null ? event.getInitiator().getId() : null;

        if (!Objects.equals(eventInitiatorId, initiatorId)) {
            throw new IllegalStateException("Только инициатор события может подтвердить запрос");
        }

        req.setStatus(RequestStatus.CONFIRMED);
        req = requestRepository.save(req);
        return toDto(req);
    }

    public ParticipationRequestDto approveOrReject(Long requestId, Long initiatorId, RequestStatus status) {
        ParticipationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        Event event = req.getEvent();
        Long eventInitiatorId = event.getInitiator() != null ? event.getInitiator().getId() : null;

        if (!Objects.equals(eventInitiatorId, initiatorId)) {
            throw new IllegalStateException("Только инициатор события может изменить статус запроса");
        }

        req.setStatus(status);
        req = requestRepository.save(req);
        return toDto(req);
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        if (!Objects.equals(req.getRequesterId(), userId)) {
            throw new IllegalStateException("Пользователь может отменять только свои заявки");
        }

        if (req.getStatus() == RequestStatus.CONFIRMED) {
            throw new IllegalStateException("Нельзя отменить подтверждённую заявку");
        }

        req.setStatus(RequestStatus.CANCELLED);
        req = requestRepository.save(req);
        return toDto(req);
    }

    private ParticipationRequestDto toDto(ParticipationRequest r) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(r.getId());
        dto.setCreated(r.getCreated());
        dto.setEventId(r.getEvent().getId());
        dto.setRequesterId(r.getRequesterId());
        dto.setComment(r.getComment());
        dto.setStatus(r.getStatus());
        return dto;
    }

    public Page<ParticipationRequestDto> getRequestsByUserAndEvent(Long userId, Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        Page<ParticipationRequest> requests = requestRepository.findAllByRequesterIdAndEventId(userId, eventId, pageable);
        return requests.map(this::toDto);
    }
}
