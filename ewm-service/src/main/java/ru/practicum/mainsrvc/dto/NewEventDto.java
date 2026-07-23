package ru.practicum.mainsrvc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public class NewEventDto {
    @NotBlank
    private String title;

    private String annotation;

    private String description;

    @NotNull
    private LocalDateTime eventDate;

    @NotNull
    @PositiveOrZero
    private Integer participantLimit;

    @NotNull
    private Boolean pinned;

    @NotNull
    private Boolean paid;

    @NotNull
    private Boolean isRequestModeration;

    @NotNull
    private Long categoryId;

    @NotNull(message = "userId обязателен для создания события")
    private Long userId;

    public NewEventDto(String title, String annotation, String description, LocalDateTime eventDate,
                       Integer participantLimit, Boolean pinned, Boolean paid, Boolean isRequestModeration,
                       Long categoryId, Long userId) {
        this.title = title;
        this.annotation = annotation;
        this.description = description;
        this.eventDate = eventDate;
        this.participantLimit = participantLimit;
        this.pinned = pinned;
        this.paid = paid;
        this.isRequestModeration = isRequestModeration;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    public NewEventDto() {
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
        this.isRequestModeration = requestModeration;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    // <-- ДОБАВЛЕНО
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
