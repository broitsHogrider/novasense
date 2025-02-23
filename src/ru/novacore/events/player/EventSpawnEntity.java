package ru.novacore.events.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.entity.Entity;
import ru.novacore.events.Event;

@Data
@AllArgsConstructor
public class EventSpawnEntity extends Event {
    private Entity entity;
}
