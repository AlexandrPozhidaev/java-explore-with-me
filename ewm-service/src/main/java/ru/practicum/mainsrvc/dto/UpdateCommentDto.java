package ru.practicum.mainsrvc.dto;

import jakarta.validation.constraints.Size;

public class UpdateCommentDto {
    @Size(min = 1, max = 2000, message = "Комментарий должен быть от 1 до 2000 символов")
    private String text;

    public UpdateCommentDto() {
    }

    public UpdateCommentDto(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
