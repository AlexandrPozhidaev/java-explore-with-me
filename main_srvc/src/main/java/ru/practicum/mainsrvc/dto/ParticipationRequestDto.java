package ru.practicum.mainsrvc.dto;

import ru.practicum.mainsrvc.entity.RequestStatus;

import java.time.LocalDateTime;

public class ParticipationRequestDto {
    private Long id;
    private Long requesterId;
    private Long eventId;
    private String comment;
    private RequestStatus status;
    private LocalDateTime created;

    public ParticipationRequestDto(Long id, Long requesterId, Long eventId, String comment, RequestStatus status, LocalDateTime created) {
        this.id = id;
        this.requesterId = requesterId;
        this.eventId = eventId;
        this.comment = comment;
        this.status = status;
        this.created = created;
    }

    public ParticipationRequestDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
