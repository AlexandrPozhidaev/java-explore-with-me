package ru.practicum.mainsrvc.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class EventShortDto {
    private Long id;
    private String title;

    @NotNull
    private Boolean pinned;

    @NotNull
    private Boolean paid;

    private Long views;

    private LocalDateTime eventDate;
    private CategoryDto category;
    private UserShortDto initiator;

    public EventShortDto(Long id, String title, Boolean pinned, Boolean paid, Long views, LocalDateTime eventDate, CategoryDto category, UserShortDto initiator) {
        this.id = id;
        this.title = title;
        this.pinned = pinned;
        this.paid = paid;
        this.views = views;
        this.eventDate = eventDate;
        this.category = category;
        this.initiator = initiator;
    }

    public EventShortDto() {
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

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
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
}
