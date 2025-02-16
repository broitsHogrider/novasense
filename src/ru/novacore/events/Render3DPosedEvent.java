package ru.novacore.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.eventbus.api.Event;
//dsad
public final class Render3DPosedEvent extends Event {
    private final MatrixStack matrix;
    private final Matrix4f projectionMatrix;
    private final ActiveRenderInfo activeRenderInfo;
    private final WorldRenderer context;
    private final float partialTicks;
    private final long finishTimeNano;
    private double cx;
    private double cy;
    private double cz;

    public MatrixStack getMatrix() {
        return this.matrix;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public ActiveRenderInfo getActiveRenderInfo() {
        return this.activeRenderInfo;
    }

    public WorldRenderer getContext() {
        return this.context;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public long getFinishTimeNano() {
        return this.finishTimeNano;
    }

    public double getCx() {
        return this.cx;
    }

    public double getCy() {
        return this.cy;
    }

    public double getCz() {
        return this.cz;
    }

    public Render3DPosedEvent(MatrixStack matrix, Matrix4f projectionMatrix, ActiveRenderInfo activeRenderInfo, WorldRenderer context, float partialTicks, long finishTimeNano, double cx, double cy, double cz) {
        this.matrix = matrix;
        this.projectionMatrix = projectionMatrix;
        this.activeRenderInfo = activeRenderInfo;
        this.context = context;
        this.partialTicks = partialTicks;
        this.finishTimeNano = finishTimeNano;
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
    }
}
