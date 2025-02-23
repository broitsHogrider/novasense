package ru.novacore.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.novacore.events.Event;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDisplay extends Event {
    MatrixStack matrixStack;
    float partialTicks;
    Type type;

    public EventDisplay(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public enum Type {
        PRE, POST, HIGH, WORLD3D
    }
}
