package ru.practicum.mainsrvc.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class EventFullDto {
    private Long id;

    @Size(min = 1, max = 255)
    private String title;

    private String annotation;
    private String description;

    private LocalDateTime eventDate;
    private Integer participantLimit;

    @NotNull
    private Boolean pinned;

    @NotNull
    private Boolean paid;

    @NotNull
    private Boolean isRequestModeration;

    private Long views;

    private CategoryDto category;
    private UserShortDto initiator;

    private Long confirmedRequests;

    public EventFullDto(Long id, String title, String annotation, String description, LocalDateTime eventDate, Integer participantLimit, Boolean pinned, Boolean paid, Boolean isRequestModeration, Long views, CategoryDto category, UserShortDto initiator, Long confirmedRequests) {
        this.id = id;
        this.title = title;
        this.annotation = annotation;
        this.description = description;
        this.eventDate = eventDate;
        this.participantLimit = participantLimit;
        this.pinned = pinned;
        this.paid = paid;
        this.isRequestModeration = isRequestModeration;
        this.views = views;
        this.category = category;
        this.initiator = initiator;
        this.confirmedRequests = confirmedRequests;
    }

    public EventFullDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    public UserShortDto getInitiator() {
        return initiator;
    }

    public void setInitiator(UserShortDto initiator) {
        this.initiator = initiator;
    }

    public Long getConfirmedRequests() {
        return confirmedRequests;
    }

    public void setConfirmedRequests(Long confirmedRequests) {
        this.confirmedRequests = confirmedRequests;
    }
}
