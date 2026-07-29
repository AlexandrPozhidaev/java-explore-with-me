package ru.practicum.mainsrvc.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ParticipationRequestDto {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private String status;

    private String comment;

    private EventShortDto event;

    private UserShortDto requester;

    public Long getId() { return id; }
    public LocalDateTime getCreated() { return created; }
    public String getStatus() { return status; }
    public String getComment() { return comment; }
    public EventShortDto getEvent() { return event; }
    public UserShortDto getRequester() { return requester; }

    public void setId(Long id) { this.id = id; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public void setStatus(String status) { this.status = status; }
    public void setComment(String comment) { this.comment = comment; }
    public void setEvent(EventShortDto event) { this.event = event; }
    public void setRequester(UserShortDto requester) { this.requester = requester; }
}