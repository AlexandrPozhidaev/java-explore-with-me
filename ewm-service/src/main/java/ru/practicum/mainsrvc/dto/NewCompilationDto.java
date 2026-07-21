package ru.practicum.mainsrvc.dto;

import jakarta.validation.constraints.NotBlank;

public class NewCompilationDto {
    @NotBlank
    private String title;

    private String description;
    private boolean pinned;

    public NewCompilationDto(String title, String description, boolean pinned) {
        this.title = title;
        this.description = description;
        this.pinned = pinned;
    }

    public NewCompilationDto() {
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

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
