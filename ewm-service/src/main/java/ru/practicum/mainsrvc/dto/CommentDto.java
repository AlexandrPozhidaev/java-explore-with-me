package ru.practicum.mainsrvc.dto;

import java.time.LocalDateTime;

public class CommentDto {
    private Long id;
    private String text;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String status;
    private Long eventId;
    private UserShortDto author;
    private String moderatorComment;

    public CommentDto() {
    }

    public CommentDto(Long id, String text, LocalDateTime created, LocalDateTime updated,
                      String status, Long eventId, UserShortDto author,
                      String moderatorComment) {
        this.id = id;
        this.text = text;
        this.created = created;
        this.updated = updated;
        this.status = status;
        this.eventId = eventId;
        this.author = author;
        this.moderatorComment = moderatorComment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public UserShortDto getAuthor() {
        return author;
    }

    public void setAuthor(UserShortDto author) {
        this.author = author;
    }

    public String getModeratorComment() {
        return moderatorComment;
    }

    public void setModeratorComment(String moderatorComment) {
        this.moderatorComment = moderatorComment;
    }
}