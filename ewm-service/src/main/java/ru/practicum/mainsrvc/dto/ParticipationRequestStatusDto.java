package ru.practicum.mainsrvc.dto;

import ru.practicum.mainsrvc.entity.RequestStatus;

public class ParticipationRequestStatusDto {

    private Long requestId;

    private RequestStatus status;

    public ParticipationRequestStatusDto() {
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
