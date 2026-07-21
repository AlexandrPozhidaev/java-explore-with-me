package ru.practicum.mainsrvc.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public class UpdateEventRequestDto {
    private String title;
    private String annotation;
    private String description;
    private LocalDateTime eventDate;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean pinned;
    private Boolean paid;
    private Boolean isRequestModeration;
    private Long categoryId;

    public UpdateEventRequestDto(String title, String annotation, String description, LocalDateTime eventDate, Integer participantLimit, Boolean pinned, Boolean paid, Boolean isRequestModeration, Long categoryId) {
        this.title = title;
        this.annotation = annotation;
        this.description = description;
        this.eventDate = eventDate;
        this.participantLimit = participantLimit;
        this.pinned = pinned;
        this.paid = paid;
        this.isRequestModeration = isRequestModeration;
        this.categoryId = categoryId;
    }

    public UpdateEventRequestDto() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public Integer getParticipantLimit() {
        return participantLimit;
    }

    public void setParticipantLimit(Integer participantLimit) {
        this.participantLimit = participantLimit;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Boolean getRequestModeration() {
        return isRequestModeration;
    }

    public void setRequestModeration(Boolean requestModeration) {
        isRequestModeration = requestModeration;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
