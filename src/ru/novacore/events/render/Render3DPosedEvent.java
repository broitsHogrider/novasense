package ru.novacore.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.vector.Matrix4f;
import ru.novacore.events.Event;

public final class Render3DPosedEvent extends Event {
    public float partialTicks;
    public MatrixStack matrixStack;
    public MainWindow scaledResolution;
    public Matrix4f matrix4f;
    public ActiveRenderInfo activeRenderInfo;

    public Render3DPosedEvent(float partialTicks, MatrixStack matrixStack, MainWindow mainWindow, Matrix4f matrix4f, ActiveRenderInfo activeRenderInfo) {
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;
        this.scaledResolution = mainWindow;
        this.matrix4f = matrix4f;
        this.activeRenderInfo = activeRenderInfo;
    }
}
