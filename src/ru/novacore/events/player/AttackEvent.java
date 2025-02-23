package ru.novacore.events.player;

import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;
import ru.novacore.events.Event;

@AllArgsConstructor
public class AttackEvent extends Event {
    public Entity entity;
}
