package ru.practicum.mainsrvc.dto;

public class UpdateCompilationDto {
    private String title;
    private String description;
    private Boolean pinned;

    public UpdateCompilationDto(String title, String description, Boolean pinned) {
        this.title = title;
        this.description = description;
        this.pinned = pinned;
    }

    public UpdateCompilationDto() {
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

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }
}
