package ru.novacore.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.Getter;
import lombok.Setter;
import ru.novacore.events.Event;

@Getter
@Setter
public class WorldEvent extends Event {
    private MatrixStack stack;
    private float partialTicks;
    
    public WorldEvent(MatrixStack stack, float partialTicks)
    {
        this.stack = stack;
        this.partialTicks = partialTicks;
    }
    
  
}
