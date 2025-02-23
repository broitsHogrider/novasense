package ru.novacore.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.novacore.events.Event;

@AllArgsConstructor
@Getter
@Setter
public class ActionEvent extends Event {
    private boolean sprintState;
}