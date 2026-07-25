package ru.practicum.mainsrvc.dto;

import jakarta.validation.constraints.NotNull;
import ru.practicum.mainsrvc.entity.EventAction;

public class StateActionDto {
    @NotNull(message = "Действие обязательно")
    private EventAction stateAction;

    public EventAction getStateAction() {
        return stateAction;
    }

    public void setStateAction(EventAction stateAction) {
        this.stateAction = stateAction;
    }
}