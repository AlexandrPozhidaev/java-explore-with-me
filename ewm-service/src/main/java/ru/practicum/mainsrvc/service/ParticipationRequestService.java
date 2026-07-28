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

    public ParticipationRequestService(RequestRepository requestRepository,
                                       EventRepository eventRepository,
                                       UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
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
        Integer participantLimit = event.getParticipantLimit();

        if (participantLimit != null && participantLimit > 0 && confirmedCount >= participantLimit) {
            throw new ConflictException("Достигнут лимит участников для этого события");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequesterId(userId);
        request.setComment(null);

        if (participantLimit != null && participantLimit == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else if (!event.getRequestModeration()) {
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

    public ParticipationRequestDto approveRequest(Long requestId, Long initiatorId) {
        return approveOrReject(requestId, initiatorId, RequestStatus.CONFIRMED);
    }

    public ParticipationRequestDto rejectRequest(Long requestId, Long initiatorId) {
        return approveOrReject(requestId, initiatorId, RequestStatus.REJECTED);
    }

    public ParticipationRequestDto approveOrReject(Long requestId, Long initiatorId, RequestStatus status) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId не может быть null");
        }
        if (initiatorId == null) {
            throw new IllegalArgumentException("initiatorId не может быть null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status не может быть null");
        }

        ParticipationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        Event event = req.getEvent();
        Long eventInitiatorId = event.getInitiator() != null ? event.getInitiator().getId() : null;

        if (!Objects.equals(eventInitiatorId, initiatorId)) {
            throw new IllegalStateException("Только инициатор события может изменить статус запроса");
        }

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("Можно обрабатывать только заявки в статусе PENDING");
        }

        if (status == RequestStatus.CONFIRMED) {
            long confirmedCount = requestRepository.countConfirmedByEventId(event.getId());
            Integer participantLimit = event.getParticipantLimit();
            if (participantLimit != null && participantLimit > 0 && confirmedCount >= participantLimit) {
                throw new ConflictException("Достигнут лимит участников для этого события");
            }
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

    public Page<ParticipationRequestDto> getRequestsByUserAndEvent(Long userId, Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        Page<ParticipationRequest> requests = requestRepository.findAllByRequesterIdAndEventId(userId, eventId, pageable);
        return requests.map(this::toDto);
    }

    private ParticipationRequestDto toDto(ParticipationRequest r) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(r.getId());
        dto.setCreated(r.getCreated());
        dto.setEvent(r.getEvent().getId());
        dto.setRequester(r.getRequesterId());
        dto.setComment(r.getComment());
        dto.setStatus(r.getStatus());
        return dto;
    }
}