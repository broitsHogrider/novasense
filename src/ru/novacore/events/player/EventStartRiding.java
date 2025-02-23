package ru.novacore.events.player;

import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;
import ru.novacore.events.Event;

@AllArgsConstructor
public class EventStartRiding extends Event {

    public Entity e;

}
