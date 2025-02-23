package ru.novacore.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.novacore.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class PostMoveEvent extends Event {
    private double horizontalMove;
}
