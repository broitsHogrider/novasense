package ru.novacore.events.render;

import lombok.AllArgsConstructor;
import ru.novacore.events.Event;

@AllArgsConstructor
public class CameraEvent extends Event {

    public float partialTicks;
    
}
