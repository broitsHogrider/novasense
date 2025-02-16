package ru.novacore.utils.render;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.opengl.GL30;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.IRenderCall;

import ru.novacore.utils.client.IMinecraft;
import net.minecraft.client.shader.Framebuffer;

public class Outline implements IMinecraft {

    private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();
    private static final Framebuffer inFrameBuffer = new Framebuffer(1, 1, true, false);
    private static final Framebuffer outFrameBuffer = new Framebuffer(1, 1, true, false);

    public static void registerRenderCall(IRenderCall rc) {
        renderQueue.add(rc);
    }

    public static void draw(int radius, int color) {
        if (renderQueue.isEmpty())
            return;

        setupBuffer(inFrameBuffer);
        setupBuffer(outFrameBuffer);

        inFrameBuffer.bindFramebuffer(true);

        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute();
        }

        outFrameBuffer.bindFramebuffer(true);

        ShaderUtils.OUTLINE.attach();
        ShaderUtils.OUTLINE.setUniformf("size", radius);
        ShaderUtils.OUTLINE.setUniform("textureIn", 0);
        ShaderUtils.OUTLINE.setUniform("textureToCheck", 20);
        ShaderUtils.OUTLINE.setUniformf("texelSize", 1.0F / (float) mc.getMainWindow().getWidth(),
                1.0F / (float) mc.getMainWindow().getHeight());
        ShaderUtils.OUTLINE.setUniformf("direction", 1.0F, 0.0F);
        float[] col = ColorUtils.rgba(color);
        ShaderUtils.OUTLINE.setUniformf("color", col[0], col[1], col[2]);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
        GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);

        inFrameBuffer.bindFramebufferTexture();
        ShaderUtils.drawQuads();

        mc.getFramebuffer().bindFramebuffer(false);
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        ShaderUtils.OUTLINE.setUniformf("direction", 0.0F, 1.0F);

        outFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE20);
        inFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        ShaderUtils.drawQuads();

        ShaderUtils.OUTLINE.detach();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    public static Framebuffer setupBuffer(Framebuffer frameBuffer) {
        if (frameBuffer.framebufferWidth != mc.getMainWindow().getWidth()
                || frameBuffer.framebufferHeight != mc.getMainWindow().getHeight())
            frameBuffer.resize(Math.max(1, mc.getMainWindow().getWidth()), Math.max(1, mc.getMainWindow().getHeight()),
                    false);
        else
            frameBuffer.framebufferClear(false);
        frameBuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);

        return frameBuffer;
    }

}