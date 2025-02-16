package ru.novacore.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.advancements.ICriterionInstance;

@Data
@AllArgsConstructor
public class EventMotion extends CancelEvent  {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;

    Runnable postMotion;
}
