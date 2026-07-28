package ru.practicum.mainsrvc.dto;

import java.util.List;

public class CompilationDto {
    private Long id;
    private Boolean pinned;
    private String title;
    private String description;
    private List<EventShortDto> events;  // Изменено с List<Long> на List<EventShortDto>

    public CompilationDto() {
    }

    public CompilationDto(Long id, Boolean pinned, String title,
                          String description, List<EventShortDto> events) {
        this.id = id;
        this.pinned = pinned;
        this.title = title;
        this.description = description;
        this.events = events;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EventShortDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventShortDto> events) {
        this.events = events;
    }
}