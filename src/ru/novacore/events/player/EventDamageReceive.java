package ru.novacore.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.novacore.events.Event;

@AllArgsConstructor
@Getter
public class EventDamageReceive extends Event {
    private final DamageType damageType;

    public enum DamageType {
        FALL,
        ARROW,
        ENDER_PEARL
    }
}