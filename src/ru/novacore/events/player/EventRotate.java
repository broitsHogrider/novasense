package ru.novacore.events.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.novacore.events.CancelEvent;

@Data
@AllArgsConstructor
public class EventRotate extends CancelEvent {
    private double yaw, pitch;
}
