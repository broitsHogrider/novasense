package ru.novacore.events.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.novacore.events.CancelEvent;

@Data
@AllArgsConstructor
public class EventMotion extends CancelEvent {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;

    Runnable postMotion;
}
