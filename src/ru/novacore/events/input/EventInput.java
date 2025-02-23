package ru.novacore.events.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.novacore.events.Event;

@Data
@AllArgsConstructor
public class EventInput extends Event {
    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakSlowDownMultiplier;
}
