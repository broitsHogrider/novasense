package ru.novacore.events.player;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Item;
import ru.novacore.events.Event;


@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class EventCooldown extends Event {
    Item item;
    CooldownType cooldownType;


    public boolean isAdded() {
        return cooldownType == CooldownType.ADDED;
    }

    public boolean isRemoved() {
        return cooldownType == CooldownType.REMOVED;
    }

    public enum CooldownType {
        ADDED, REMOVED
    }
}
