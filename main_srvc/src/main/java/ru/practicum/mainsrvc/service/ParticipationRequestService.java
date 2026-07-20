package ru.practicum.mainsrvc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainsrvc.dto.CreateRequestDto;
import ru.practicum.mainsrvc.dto.ParticipationRequestDto;
import ru.practicum.mainsrvc.entity.Event;
import ru.practicum.mainsrvc.entity.ParticipationRequest;
import ru.practicum.mainsrvc.entity.RequestStatus;
import ru.practicum.mainsrvc.repository.EventRepository;
import ru.practicum.mainsrvc.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParticipationRequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    public ParticipationRequestService(RequestRepository requestRepository, EventRepository eventRepository) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
    }

    public ParticipationRequestDto createRequest(Long userId, Long eventId, CreateRequestDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequesterId(userId);
        request.setComment(dto.getComment());
        request.setStatus(RequestStatus.PENDING);

        request = requestRepository.save(request);
        return toDto(request);
    }

    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        var requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ParticipationRequestDto> getRequestsForEvent(Long eventId) {
        var requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ParticipationRequestDto approveRequestByInitiator(Long requestId, Long initiatorId) {
        ParticipationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        Event event = req.getEvent();
        if (!event.getInitiator().equals(initiatorId)) {
            throw new IllegalStateException("Only event initiator can approve requests");
        }

        req.setStatus(RequestStatus.CONFIRMED);
        req = requestRepository.save(req);
        return toDto(req);
    }

    public ParticipationRequestDto rejectRequestByInitiator(Long requestId, Long initiatorId) {
        ParticipationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        Event event = req.getEvent();
        if (!event.getInitiator().equals(initiatorId)) {
            throw new IllegalStateException("Only event initiator can reject requests");
        }

        req.setStatus(RequestStatus.REJECTED);
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
}
