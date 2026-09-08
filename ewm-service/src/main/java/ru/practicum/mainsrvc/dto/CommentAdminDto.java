package ru.practicum.mainsrvc.dto;

import ru.practicum.mainsrvc.entity.CommentStatus;

public class CommentAdminDto {

    private CommentStatus status;
    private String moderatorComment;

    public CommentAdminDto() {
    }

    public CommentAdminDto(CommentStatus status, String moderatorComment) {
        this.status = status;
        this.moderatorComment = moderatorComment;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public String getModeratorComment() {
        return moderatorComment;
    }

    public void setModeratorComment(String moderatorComment) {
        this.moderatorComment = moderatorComment;
    }
}
