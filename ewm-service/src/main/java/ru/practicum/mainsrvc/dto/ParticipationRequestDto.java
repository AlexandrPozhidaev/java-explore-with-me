package ru.practicum.mainsrvc.dto;

import ru.practicum.mainsrvc.entity.RequestStatus;

import java.time.LocalDateTime;

public class ParticipationRequestDto {
    private Long id;
    private Long requester;
    private Long eventId;
    private String comment;
    private RequestStatus status;
    private LocalDateTime created;

    public ParticipationRequestDto(Long id, Long requester, Long event, String comment, RequestStatus status, LocalDateTime created) {
        this.id = id;
        this.requester = requester;
        this.eventId = event;
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

    public Long getRequester() {        // исправлено: было getRequesterId()
        return requester;
    }

    public void setRequester(Long requester) {   // исправлено: было setRequesterId
        this.requester = requester;
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
