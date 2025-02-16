package ru.novacore.utils.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jhlabs.image.GaussianFilter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.optifine.util.TextureUtils;
import org.joml.Vector2d;
import org.joml.Vector4i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import ru.novacore.ui.styles.Style;
import ru.novacore.utils.client.IMinecraft;
import ru.novacore.utils.math.MathUtil;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Objects;

import static com.mojang.blaze3d.platform.GlStateManager.*;
import static com.mojang.blaze3d.systems.RenderSystem.enableBlend;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static ru.novacore.utils.render.RenderUtils.IntColor.*;

public class RenderUtils implements IMinecraft {

    private static final HashMap<Integer, Integer> shadowCache1 = new HashMap<>();
    private static final HashMap<Integer, Integer> shadowCache2 = new HashMap<>();

    public static boolean isInRegion(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static boolean isInRegion(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static boolean isInRegion(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void color(int rgb) {
        GL11.glColor3f(getRed(rgb) / 255f, getGreen(rgb) / 255f, getBlue(rgb) / 255f);
    }

    public static class IntColor {

        public static float[] rgb(int color) {
            return new float[]{(color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f};
        }

        public static int rgba(int r, int g, int b, int a) {
            return a << 24 | r << 16 | g << 8 | b;
        }

        public static int getRed(int hex) {
            return hex >> 16 & 255;
        }

        public static int getGreen(int hex) {
            return hex >> 8 & 255;
        }

        public static int getBlue(int hex) {
            return hex & 255;
        }

        public static int getAlpha(final int hex) {
            return hex >> 24 & 255;
        }
    }

    public static class Render2D {

        public static void drawCustomRound(float x, float y, float width, float height, float radius) {
            drawCustomRound(x, y, width, height, radius, 255);
        }


        public static void drawRoundCircle(float x,
                                           float y,
                                           float radius,
                                           int color) {
            drawRound(x - (radius / 2), y - (radius / 2), radius, radius, (radius / 2) - 0.5f, color);
        }

        public static void drawCustomRound(float x, float y, float width, float height, float radius, float alpha) {
            int firstColor = ColorUtils.applyOpacity(ColorUtils.getColor(0), alpha);
            int secondColor = ColorUtils.applyOpacity(ColorUtils.getColor(90), alpha);
            int thirdColor = ColorUtils.applyOpacity(ColorUtils.getColor(180), alpha);
            int fourthColor = ColorUtils.applyOpacity(ColorUtils.getColor(270), alpha);
            float offset = 3;

            drawGradientGlowRound(x - offset, y - offset, width + (offset * 2), height + (offset * 2), radius, offset, firstColor, secondColor, thirdColor, fourthColor);
            drawGradientRound(x, y, width, height, new Vector4f(radius, radius, radius, radius), firstColor, secondColor, thirdColor, fourthColor);
        }

        public static void drawRoundWithGlow(float x, float y, float width, float height, float radius, int color) {
            float offset = 3;

            drawGlowRound(x - offset, y - offset, width + (offset * 2), height + (offset * 2), radius, offset, color);
            drawRound(x, y, width, height, radius, color);
        }

        public static void drawGradientRoundWithGlow(float x, float y, float width, float height, float radius, int firstColor, int secondColor, int thirdColor, int fourthColor) {
            float offset = 3;

            drawGradientGlowRound(x - offset, y - offset, width + (offset * 2), height + (offset * 2), radius, offset, firstColor, secondColor, thirdColor, fourthColor);
            drawGradientRound(x, y, width, height, new Vector4f(radius, radius, radius, radius), firstColor, secondColor, thirdColor, fourthColor);
        }

        public static void drawRound(float x, float y, float width, float height, float radius, int color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            ShaderUtils.ROUND.attach();
            ShaderUtils.ROUND.setUniform("size", width * 2, height * 2);
            ShaderUtils.ROUND.setUniform("radius", radius * 2);
            ShaderUtils.ROUND.setUniform("color", getRed(color) / 255f, getGreen(color) / 255f, getBlue(color) / 255f, getAlpha(color) / 255f);
            quadsBegin(x, y, width, height, 7);
            ShaderUtils.ROUND.detach();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawRoundOutline(float x, float y, float width, float height, float radius, float outlineThickness, int color, Vector4i outlineColor) {
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            ShaderUtils.ROUND_SHADER_OUTLINE.attach();

            MainWindow sr = mc.getMainWindow();

            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("location", (float) (x * 2),
                    (float) ((sr.getHeight() - (height * 2)) - (y * 2)));
            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("rectSize", (float) (width * 2), (float) (height * 2));
            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("radius", (float) (radius * 2));

            float[] clr = IntColor.rgb(color);
            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("outlineThickness", (float) (outlineThickness * 2));
            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("color", clr[0], clr[1], clr[2],clr[3]);

            for (int i = 0; i < 4;i++) {
                float[] col = IntColor.rgb(outlineColor.get(i));
                ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("outlineColor" + (i + 1), col[0], col[1], col[2],col[3]);
            }

            quadsBegin(x - (2 + outlineThickness), y - (2 + outlineThickness), width + (4 + outlineThickness * 2), height + (4 + outlineThickness * 2), 7);
            ShaderUtils.ROUND_SHADER_OUTLINE.detach();
            GlStateManager.disableBlend();
        }

        public static void drawGradientRound(float x, float y, float width, float height, Vector4f radius, int firstColor, int secondColor, int thirdColor, int fourthColor) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            ShaderUtils.GRADIENT_ROUND.attach();
            ShaderUtils.GRADIENT_ROUND.setUniform("rectSize", width * 2, height * 2);
            ShaderUtils.GRADIENT_ROUND.setUniform("cornerRadii", radius.x * 2, radius.y * 2, radius.z * 2, radius.w * 2);
            ShaderUtils.GRADIENT_ROUND.setUniform("color1", getRed(firstColor) / 255f, getGreen(firstColor) / 255f, getBlue(firstColor) / 255f, getAlpha(firstColor) / 255f);
            ShaderUtils.GRADIENT_ROUND.setUniform("color2", getRed(secondColor) / 255f, getGreen(secondColor) / 255f, getBlue(secondColor) / 255f, getAlpha(secondColor) / 255f);
            ShaderUtils.GRADIENT_ROUND.setUniform("color3", getRed(thirdColor) / 255f, getGreen(thirdColor) / 255f, getBlue(thirdColor) / 255f, getAlpha(thirdColor) / 255f);
            ShaderUtils.GRADIENT_ROUND.setUniform("color4", getRed(fourthColor) / 255f, getGreen(fourthColor) / 255f, getBlue(fourthColor) / 255f, getAlpha(fourthColor) / 255f);
            quadsBegin(x, y, width, height, 7);
            ShaderUtils.GRADIENT_ROUND.detach();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawVectorRound(float x, float y, float width, float height, Vector4f radius, int color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            ShaderUtils.VECTOR_ROUND.attach();
            ShaderUtils.VECTOR_ROUND.setUniform("size", width * 2, height * 2);
            ShaderUtils.VECTOR_ROUND.setUniform("radius", radius.x * 2, radius.y * 2, radius.z * 2, radius.w * 2);
            ShaderUtils.VECTOR_ROUND.setUniform("color", getRed(color) / 255f, getGreen(color) / 255f, getBlue(color) / 255f, getAlpha(color) / 255f);
            quadsBegin(x, y, width, height, 7);
            ShaderUtils.VECTOR_ROUND.detach();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawCornerRound(float x, float y, float width, float height, float radius, int color, Corner corner) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            ShaderUtils.VECTOR_ROUND.attach();
            ShaderUtils.VECTOR_ROUND.setUniform("size", width * 2, height * 2);
            switch (corner) {
                case ALL -> ShaderUtils.VECTOR_ROUND.setUniform("radius", radius * 2, radius * 2, radius * 2, radius * 2);
                case RIGHT -> ShaderUtils.VECTOR_ROUND.setUniform("radius", 0, 0, radius * 2, radius * 2);
                case LEFT -> ShaderUtils.VECTOR_ROUND.setUniform("radius", radius * 2, radius * 2, 0, 0);
                case TOP_RIGHT -> ShaderUtils.VECTOR_ROUND.setUniform("radius", 0, 0, radius * 2, 0);
                case TOP -> ShaderUtils.VECTOR_ROUND.setUniform("radius", radius * 2, 0, radius * 2, 0);
                case DOWN -> ShaderUtils.VECTOR_ROUND.setUniform("radius", 0, radius * 2, 0, radius * 2);
            }
            ShaderUtils.VECTOR_ROUND.setUniform("color", getRed(color) / 255f, getGreen(color) / 255f, getBlue(color) / 255f, getAlpha(color) / 255f);
            quadsBegin(x, y, width, height, 7);
            ShaderUtils.VECTOR_ROUND.detach();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawGlowRound(float x, float y, float width, float height, float radius, float glowRadius, int color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.disableAlphaTest();
            ShaderUtils.GLOW_ROUND.attach();
            ShaderUtils.GLOW_ROUND.setUniform("size", width * 2, height * 2);
            ShaderUtils.GLOW_ROUND.setUniform("radius", radius * 2);
            ShaderUtils.GLOW_ROUND.setUniform("glowRadius", glowRadius * 2);
            ShaderUtils.GLOW_ROUND.setUniform("color", getRed(color) / 255f, getGreen(color) / 255f, getBlue(color) / 255f, getAlpha(color) / 255f);
            quadsBegin(x, y, width, height, 7);
            ShaderUtils.GLOW_ROUND.detach();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawGradientGlowRound(float x, float y, float width, float height, float radius, float glowRadius, int firstColor, int secondColor, int thirdColor, int fourthColor) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.disableAlphaTest();
            ShaderUtils.GRADIENT_GLOW_ROUND.attach();
            ShaderUtils.GRADIENT_GLOW_ROUND.setUniform("size", width * 2, height * 2);
            ShaderUtils.GRADIENT_GLOW_ROUND.setUniform("radius", radius * 2);
            ShaderUtils.GRADIENT_GLOW_ROUND.setUniform("glowRadius", glowRadius * 2);
            ShaderUtils.GRADIENT_GLOW_ROUND.setUniform("color1", getRed(firstColor) / 255f, getGreen(firstColor) / 255f, getBlue(firstColor) / 255f, getAlpha(firstColor) / 255f);
            ShaderUtils.GRADIENT_GLOW_ROUND.setUniform("color2", getRed(secondColor) / 255f, getGreen(secondColor) / 255f, getBlue(secondColor) / 255f, getAlpha(secondColor) / 255f);
            ShaderUtils.GRADIENT_GLOW_ROUND.setUniform("color3", getRed(thirdColor) / 255f, getGreen(thirdColor) / 255f, getBlue(thirdColor) / 255f, getAlpha(thirdColor) / 255f);
            ShaderUtils.GRADIENT_GLOW_ROUND.setUniform("color4", getRed(fourthColor) / 255f, getGreen(fourthColor) / 255f, getBlue(fourthColor) / 255f, getAlpha(fourthColor) / 255f);
            quadsBegin(x, y, width, height, 7);
            ShaderUtils.GRADIENT_GLOW_ROUND.detach();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawShadow(float x, float y, float width, float height, int radius, int color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.01f);
            RenderSystem.disableAlphaTest();

            x -= radius;
            y -= radius;
            width = width + radius * 2;
            height = height + radius * 2;
            x -= 0.25f;
            y += 0.25f;

            int identifier = Objects.hash(width, height, radius);
            int textureId;

            if (shadowCache1.containsKey(identifier)) {
                textureId = shadowCache1.get(identifier);
                RenderSystem.bindTexture(textureId);
            } else {
                if (width <= 0) width = 1;
                if (height <= 0) height = 1;

                BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D graphics = originalImage.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
                graphics.dispose();

                GaussianFilter filter = new GaussianFilter(radius);
                BufferedImage blurredImage = filter.filter(originalImage, null);
                DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
                texture.setBlurMipmap(true, true);
                try {
                    textureId = texture.getGlTextureId();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                shadowCache1.put(identifier, textureId);
            }

            float[] startColorComponents = IntColor.rgb(color);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
            buffer.pos(x, y, 0).color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3]).tex(0, 0).endVertex();
            buffer.pos(x, y + (float) ((int) height), 0).color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3]).tex(0, 1).endVertex();
            buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0).color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3]).tex(1, 1).endVertex();
            buffer.pos(x + (float) ((int) width), y, 0).color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3]).tex(1, 0).endVertex();
            tessellator.draw();

            RenderSystem.enableAlphaTest();
            RenderSystem.bindTexture(0);
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawShadow(float x, float y, float width, float height, int radius, int startColor, int endColor) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.01f);

            x -= radius;
            y -= radius;
            width = width + radius * 2;
            height = height + radius * 2;
            x -= 0.25f;
            y += 0.25f;

            int identifier = Objects.hash(width, height, radius);
            int textureId;

            if (shadowCache1.containsKey(identifier)) {
                textureId = shadowCache1.get(identifier);
                RenderSystem.bindTexture(textureId);
            } else {
                if (width <= 0) width = 1;
                if (height <= 0) height = 1;

                BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D graphics = originalImage.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
                graphics.dispose();

                GaussianFilter filter = new GaussianFilter(radius);
                BufferedImage blurredImage = filter.filter(originalImage, null);
                DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
                texture.setBlurMipmap(true, true);
                try {
                    textureId = texture.getGlTextureId();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                shadowCache1.put(identifier, textureId);
            }

            float[] startColorComponents = IntColor.rgb(startColor);
            float[] endColorComponents = IntColor.rgb(endColor);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
            buffer.pos(x, y, 0).color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3]).tex(0, 0).endVertex();
            buffer.pos(x, y + (float) ((int) height), 0).color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3]).tex(0, 1).endVertex();
            buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0).color(endColorComponents[0], endColorComponents[1], endColorComponents[2], endColorComponents[3]).tex(1, 1).endVertex();
            buffer.pos(x + (float) ((int) width), y, 0).color(endColorComponents[0], endColorComponents[1], endColorComponents[2], endColorComponents[3]).tex(1, 0).endVertex();
            tessellator.draw();

            RenderSystem.bindTexture(0);
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawShadow(float x, float y, float width, float height, int radius, int bottomLeft, int topLeft, int bottomRight, int topRight) {
            RenderSystem.pushMatrix();
            RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA.param, DestFactor.ONE_MINUS_SRC_ALPHA.param, SourceFactor.ONE.param, DestFactor.ZERO.param);
            RenderSystem.shadeModel(7425);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.01f);

            x -= radius;
            y -= radius;
            width = width + radius * 2;
            height = height + radius * 2;
            x -= 0.25f;
            y += 0.25f;

            int identifier = Objects.hash(width, height, radius);
            int textureId;

            if (shadowCache1.containsKey(identifier)) {
                textureId = shadowCache1.get(identifier);
                RenderSystem.bindTexture(textureId);
            } else {
                if (width <= 0) width = 1;
                if (height <= 0) height = 1;

                BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D graphics = originalImage.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
                graphics.dispose();

                GaussianFilter filter = new GaussianFilter(radius);
                BufferedImage blurredImage = filter.filter(originalImage, null);
                DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
                texture.setBlurMipmap(true, true);
                try {
                    textureId = texture.getGlTextureId();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                shadowCache1.put(identifier, textureId);
            }

            float[] bottomLefts = IntColor.rgb(bottomLeft);
            float[] topLefts = IntColor.rgb(topLeft);
            float[] bottomRights = IntColor.rgb(bottomRight);
            float[] topRights = IntColor.rgb(topRight);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
            buffer.pos(x, y, 0).color(bottomLefts[0], bottomLefts[1], bottomLefts[2], bottomLefts[3]).tex(0, 0).endVertex();
            buffer.pos(x, y + (float) ((int) height), 0).color(topLefts[0], topLefts[1], topLefts[2], topLefts[3]).tex(0, 1).endVertex();
            buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0).color(topRights[0], topRights[1], topRights[2], topRights[3]).tex(1, 1).endVertex();
            buffer.pos(x + (float) ((int) width), y, 0).color(bottomRights[0], bottomRights[1], bottomRights[2], bottomRights[3]).tex(1, 0).endVertex();
            tessellator.draw();

            RenderSystem.shadeModel(7424);
            RenderSystem.bindTexture(0);
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawImage(float x, float y, float width, float height, float radius, float alpha) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            ShaderUtils.TEXTURE_ROUND.attach();
            ShaderUtils.TEXTURE_ROUND.setUniform("size", width * 2, height * 2);
            ShaderUtils.TEXTURE_ROUND.setUniform("radius", radius * 2);
            ShaderUtils.TEXTURE_ROUND.setUniform("alpha", alpha);
            quadsBegin(x, y, width, height, 7);
            ShaderUtils.TEXTURE_ROUND.detach();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height, int color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            ColorUtils.setColor(color);
            mc.getTextureManager().bindTexture(resourceLocation);
            AbstractGui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height, Vector4i color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.shadeModel(7425);
            mc.getTextureManager().bindTexture(resourceLocation);
            quadsBeginC(x, y, width, height, 7, color);
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawImage(MatrixStack stack, ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
            mc.getTextureManager().bindTexture(image);
            drawImage(stack, x, y, z, width, height, color1, color2, color3, color4);
        }

        public static void drawImage(MatrixStack stack, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
            boolean blend = GL11.glIsEnabled(3042);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 1);
            GL11.glShadeModel(7425);
            GL11.glAlphaFunc(516, 0.0F);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
            buffer.pos(stack.getLast().getMatrix(), (float)x, (float)(y + height), (float)z).color(color1 >> 16 & 255, color1 >> 8 & 255, color1 & 255, color1 >>> 24).tex(0.0F, 0.99F).lightmap(0, 240).endVertex();
            buffer.pos(stack.getLast().getMatrix(), (float)(x + width), (float)(y + height), (float)z).color(color2 >> 16 & 255, color2 >> 8 & 255, color2 & 255, color2 >>> 24).tex(1.0F, 0.99F).lightmap(0, 240).endVertex();
            buffer.pos(stack.getLast().getMatrix(), (float)(x + width), (float)y, (float)z).color(color3 >> 16 & 255, color3 >> 8 & 255, color3 & 255, color3 >>> 24).tex(1.0F, 0.0F).lightmap(0, 240).endVertex();
            buffer.pos(stack.getLast().getMatrix(), (float)x, (float)y, (float)z).color(color4 >> 16 & 255, color4 >> 8 & 255, color4 & 255, color4 >>> 24).tex(0.0F, 0.0F).lightmap(0, 240).endVertex();
            tessellator.draw();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glShadeModel(7424);
            GlStateManager.blendFunc(770, 0);
            if (!blend) {
                GlStateManager.disableBlend();
            }

        }

        public static void drawTexture(final float x,
                                       final float y,
                                       final float width,
                                       final float height,
                                       final float radius,
                                       final float alpha) {
            pushMatrix();
            enableBlend();
            blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            ShaderUtils.TEXTURE_ROUND.attach();

            ShaderUtils.TEXTURE_ROUND.setUniform("rectSize", (float) (width * 2), (float) (height * 2));
            ShaderUtils.TEXTURE_ROUND.setUniform("radius", radius * 2);
            ShaderUtils.TEXTURE_ROUND.setUniform("alpha", alpha);

            quadsBegin(x, y, width, height, 7);


            ShaderUtils.TEXTURE_ROUND.detach();
            popMatrix();
        }
        private static final Long openTime = System.currentTimeMillis();
        public static void drawMainMenuShader(float width, float height) {
            ShaderUtils.MAINMENUSHADER.attach();
            ShaderUtils.MAINMENUSHADER.setUniformf("resolution", width, height); // Set screen resolution
            ShaderUtils.MAINMENUSHADER.setUniformf("time", (float) (System.currentTimeMillis() % 100000) / 1000f); // Set time
            quadsBegin(0, 0, width, height, 7);
            ShaderUtils.MAINMENUSHADER.detach();
        }

        public static void drawTexture(MatrixStack matrixStack, ResourceLocation resourceLocation, float x, float y, float width, float height, int color1, int color2, int color3, int color4) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            RenderSystem.shadeModel(7425);
            RenderSystem.disableAlphaTest();
            RenderSystem.depthMask(false);
            Matrix4f matrix4f = matrixStack.getLast().getMatrix();
            mc.getTextureManager().bindTexture(resourceLocation);
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
            buffer.pos(matrix4f, x, y, 0).color(color1).tex(0, 0).endVertex();
            buffer.pos(matrix4f, x, y + height, 0).color(color2).tex(0, 1).endVertex();
            buffer.pos(matrix4f, x + width, y + height, 0).color(color3).tex(1, 1).endVertex();
            buffer.pos(matrix4f, x + width, y, 0).color(color4).tex(1, 0).endVertex();
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.enableAlphaTest();
            RenderSystem.shadeModel(7424);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawTexture(MatrixStack matrixStack, ResourceLocation resourceLocation, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            RenderSystem.shadeModel(7425);
            RenderSystem.disableAlphaTest();
            RenderSystem.depthMask(false);
            Matrix4f matrix4f = matrixStack.getLast().getMatrix();
            mc.getTextureManager().bindTexture(resourceLocation);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
            buffer.pos(matrix4f, (float) x, (float) (y + height), (float) z).color(color1).tex(0, 1).endVertex();
            buffer.pos(matrix4f, (float) (x + width), (float) (y + height), (float) z).color(color2).tex(1, 1).endVertex();
            buffer.pos(matrix4f, (float) (x + width), (float) y,(float) z).color(color3).tex(1, 0).endVertex();
            buffer.pos(matrix4f,(float) x,(float) y,(float) z).color(color4).tex(0, 0).endVertex();
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.enableAlphaTest();
            RenderSystem.shadeModel(7424);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawArrow(MatrixStack matrixStack, float x, float y, float size, int color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            RenderSystem.shadeModel(7425);
            RenderSystem.disableAlphaTest();
            RenderSystem.depthMask(false);
            Matrix4f matrix = matrixStack.getLast().getMatrix();
            mc.getTextureManager().bindTexture(new ResourceLocation("novacore/images/visuals/arrow.png"));
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
            buffer.pos(matrix, x - size / 2f, y + size, 0).color(color).tex(0, 1).endVertex();
            buffer.pos(matrix, x + size / 2f, y + size, 0).color(color).tex(1, 1).endVertex();
            buffer.pos(matrix, x + size / 2f, y, 0).color(color).tex(1, 0).endVertex();
            buffer.pos(matrix, x - size / 2f, y, 0).color(color).tex(0, 0).endVertex();
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.enableAlphaTest();
            RenderSystem.shadeModel(7424);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static int downloadImage(String url) {
            int texId;
            int identifier = Objects.hash(url);
            if (shadowCache2.containsKey(identifier)) {
                texId = shadowCache2.get(identifier);
            } else {
                URL stringURL;
                try {
                    stringURL = new URL(url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }

                BufferedImage img;
                try {
                    img = ImageIO.read(stringURL);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    texId = loadTexture(img);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                shadowCache2.put(identifier, texId);
            }

            return texId;
        }

        public static void drawCircle(float x, float y, float start, float end, float radius, float width, boolean filled) {
            float i;
            float endOffset;
            if (start > end) {
                endOffset = end;
                end = start;
                start = endOffset;
            }

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.disableTexture();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);

            if (filled) {
                GL11.glBegin(GL11.GL_TRIANGLE_FAN);
                for (i = end; i >= start; i--) {
                    ColorUtils.setColor(ColorUtils.getColor((int) (i * 2)));
                    float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                    float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                    GL11.glVertex2f(x + cos, y + sin);
                }
                GL11.glEnd();
            }

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (i = end; i >= start; i--) {
                ColorUtils.setColor(ColorUtils.getColor((int) (i * 2)));
                float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();

            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawCircle(float x, float y, float start, float end, float radius, float width, boolean filled, int color) {
            float i;
            float endOffset;
            if (start > end) {
                endOffset = end;
                end = start;
                start = endOffset;
            }

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.disableTexture();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);

            if (filled) {
                GL11.glBegin(GL11.GL_TRIANGLE_FAN);
                for (i = end; i >= start; i--) {
                    ColorUtils.setColor(color);
                    float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                    float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                    GL11.glVertex2f(x + cos, y + sin);
                }
                GL11.glEnd();
            }

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (i = end; i >= start; i--) {
                ColorUtils.setColor(color);
                float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();

            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawCircle(float x, float y, float radius, int color) {
            drawRound(x - radius / 2f, y - radius / 2f, radius, radius,
                    radius / 2f, color);
        }

        public static void drawShadowCircle(float x, float y, float radius, int color) {
            drawShadow(x - radius / 2f, y - radius / 2f, radius, radius,
                    (int) radius, color);
        }

        public static void drawRect(float x, float y, float width, float height, int color) {
            drawMcRect(x, y, x + width, y + height, color);
        }

        public static void drawMcRect(double left, double top, double right, double bottom, int color) {
            if (left < right) {
                double i = left;
                left = right;
                right = i;
            }

            if (top < bottom) {
                double j = top;
                top = bottom;
                bottom = j;
            }

            float f3 = (float) (color >> 24 & 255) / 255f;
            float f = (float) (color >> 16 & 255) / 255f;
            float f1 = (float) (color >> 8 & 255) / 255f;
            float f2 = (float) (color & 255) / 255f;

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.blendFunc(770, 771);
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(left, bottom, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(right, bottom, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(right, top, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(left, top, 0).color(f, f1, f2, f3).endVertex();
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawLine(double x, double y, double z, double w, int color) {
            float f3 = (float) (color >> 24 & 255) / 255f;
            float f = (float) (color >> 16 & 255) / 255f;
            float f1 = (float) (color >> 8 & 255) / 255f;
            float f2 = (float) (color & 255) / 255f;

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.blendFunc(770, 771);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            RenderSystem.lineWidth(1.5f);
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(z, w, 0).color(f, f1, f2, f3).endVertex();
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawMcRectBuilding(double left, double top, double right, double bottom, int color) {
            if (left < right) {
                double i = left;
                left = right;
                right = i;
            }

            if (top < bottom) {
                double j = top;
                top = bottom;
                bottom = j;
            }

            float f3 = (float) (color >> 24 & 255) / 255f;
            float f = (float) (color >> 16 & 255) / 255f;
            float f1 = (float) (color >> 8 & 255) / 255f;
            float f2 = (float) (color & 255) / 255f;

            buffer.pos(left, bottom, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(right, bottom, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(right, top, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(left, top, 0).color(f, f1, f2, f3).endVertex();
        }

        public static void drawRectBuilding(double left, double top, double right, double bottom, int color) {
            right += left;
            bottom += top;

            if (left < right) {
                double i = left;
                left = right;
                right = i;
            }

            if (top < bottom) {
                double j = top;
                top = bottom;
                bottom = j;
            }

            float f3 = (float) (color >> 24 & 255) / 255f;
            float f = (float) (color >> 16 & 255) / 255f;
            float f1 = (float) (color >> 8 & 255) / 255f;
            float f2 = (float) (color & 255) / 255f;

            buffer.pos(left, bottom, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(right, bottom, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(right, top, 0).color(f, f1, f2, f3).endVertex();
            buffer.pos(left, top, 0).color(f, f1, f2, f3).endVertex();
        }

        public static void drawRectOutlineBuilding(double x, double y, double width, double height, double size, int color) {
            drawMcRectBuilding(x + size, y, width - size, y + size, color);
            drawMcRectBuilding(x, y, x + size, height, color);
            drawMcRectBuilding(width - size, y, width, height, color);
            drawMcRectBuilding(x + size, height - size, width - size, height, color);
        }

        public static void drawRectOutlineBuildingGradient(double x, double y, double width, double height, double size, Vector4i colors) {
            drawMCHorizontalBuilding(x + size, y, width - size, y + size, colors.x, colors.z);
            drawMCVerticalBuilding(x, y, x + size, height, colors.z, colors.x);
            drawMCVerticalBuilding(width - size, y, width, height, colors.x, colors.z);
            drawMCHorizontalBuilding(x + size, height - size, width - size, height, colors.z, colors.x);
        }

        public static void drawMCHorizontal(double x, double y, double width, double height, int start, int end) {
            float f = (float) (start >> 24 & 255) / 255f;
            float f1 = (float) (start >> 16 & 255) / 255f;
            float f2 = (float) (start >> 8 & 255) / 255f;
            float f3 = (float) (start & 255) / 255f;
            float f4 = (float) (end >> 24 & 255) / 255f;
            float f5 = (float) (end >> 16 & 255) / 255f;
            float f6 = (float) (end >> 8 & 255) / 255f;
            float f7 = (float) (end & 255) / 255f;

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.shadeModel(7425);
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, height, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(width, y, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(x, y, 0).color(f1, f2, f3, f).endVertex();
            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawMCHorizontalBuilding(double x, double y, double width, double height, int start, int end) {
            float f = (float) (start >> 24 & 255) / 255f;
            float f1 = (float) (start >> 16 & 255) / 255f;
            float f2 = (float) (start >> 8 & 255) / 255f;
            float f3 = (float) (start & 255) / 255f;
            float f4 = (float) (end >> 24 & 255) / 255f;
            float f5 = (float) (end >> 16 & 255) / 255f;
            float f6 = (float) (end >> 8 & 255) / 255f;
            float f7 = (float) (end & 255) / 255f;

            buffer.pos(x, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, height, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(width, y, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(x, y, 0).color(f1, f2, f3, f).endVertex();
        }

        public static void drawHorizontal(float x, float y, float width, float height, int start, int end) {
            width += x;
            height += y;

            float f = (float) (start >> 24 & 255) / 255f;
            float f1 = (float) (start >> 16 & 255) / 255f;
            float f2 = (float) (start >> 8 & 255) / 255f;
            float f3 = (float) (start & 255) / 255f;
            float f4 = (float) (end >> 24 & 255) / 255f;
            float f5 = (float) (end >> 16 & 255) / 255f;
            float f6 = (float) (end >> 8 & 255) / 255f;
            float f7 = (float) (end & 255) / 255f;

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.shadeModel(7425);
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, height, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(width, y, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(x, y, 0).color(f1, f2, f3, f).endVertex();
            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawVertical(float x, float y, float width, float height, int start, int end) {
            width += x;
            height += y;

            float f = (float) (start >> 24 & 255) / 255f;
            float f1 = (float) (start >> 16 & 255) / 255f;
            float f2 = (float) (start >> 8 & 255) / 255f;
            float f3 = (float) (start & 255) / 255f;
            float f4 = (float) (end >> 24 & 255) / 255f;
            float f5 = (float) (end >> 16 & 255) / 255f;
            float f6 = (float) (end >> 8 & 255) / 255f;
            float f7 = (float) (end & 255) / 255f;

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.shadeModel(7425);
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, y, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(x, y, 0).color(f5, f6, f7, f4).endVertex();
            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawBox(double x, double y, double width, double height, double size, int color) {
            drawRectBuilding(x + size, y, width - size, y + size, color);
            drawRectBuilding(x, y, x + size, height, color);

            drawRectBuilding(width - size, y, width, height, color);
            drawRectBuilding(x + size, height - size, width - size, height, color);
        }

        public static void drawBoxTest(double x, double y, double width, double height, double size, ru.novacore.utils.math.Vector4i colors) {
            drawMCHorizontalBuilding(x + size, y, width - size, y + size, colors.x, colors.z);
            drawMCVerticalBuilding(x, y, x + size, height, colors.z, colors.x);

            drawMCVerticalBuilding(width - size, y, width, height, colors.x, colors.z);
            drawMCHorizontalBuilding(x + size, height - size, width - size, height, colors.z, colors.x);
        }

        public static void drawMCVertical(double x, double y, double width, double height, int start, int end) {
            float f = (float) (start >> 24 & 255) / 255f;
            float f1 = (float) (start >> 16 & 255) / 255f;
            float f2 = (float) (start >> 8 & 255) / 255f;
            float f3 = (float) (start & 255) / 255f;
            float f4 = (float) (end >> 24 & 255) / 255f;
            float f5 = (float) (end >> 16 & 255) / 255f;
            float f6 = (float) (end >> 8 & 255) / 255f;
            float f7 = (float) (end & 255) / 255f;

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.shadeModel(7425);
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, y, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(x, y, 0).color(f5, f6, f7, f4).endVertex();
            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawMCVerticalBuilding(double x, double y, double width, double height, int start, int end) {
            float f = (float) (start >> 24 & 255) / 255f;
            float f1 = (float) (start >> 16 & 255) / 255f;
            float f2 = (float) (start >> 8 & 255) / 255f;
            float f3 = (float) (start & 255) / 255f;
            float f4 = (float) (end >> 24 & 255) / 255f;
            float f5 = (float) (end >> 16 & 255) / 255f;
            float f6 = (float) (end >> 8 & 255) / 255f;
            float f7 = (float) (end & 255) / 255f;

            buffer.pos(x, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, y, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(x, y, 0).color(f5, f6, f7, f4).endVertex();
        }

        public static void drawVerticalBuilding(double x, double y, double width, double height, int start, int end) {
            width += x;
            height += y;

            float f = (float) (start >> 24 & 255) / 255f;
            float f1 = (float) (start >> 16 & 255) / 255f;
            float f2 = (float) (start >> 8 & 255) / 255f;
            float f3 = (float) (start & 255) / 255f;
            float f4 = (float) (end >> 24 & 255) / 255f;
            float f5 = (float) (end >> 16 & 255) / 255f;
            float f6 = (float) (end >> 8 & 255) / 255f;
            float f7 = (float) (end & 255) / 255f;

            buffer.pos(x, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, height, 0).color(f1, f2, f3, f).endVertex();
            buffer.pos(width, y, 0).color(f5, f6, f7, f4).endVertex();
            buffer.pos(x, y, 0).color(f5, f6, f7, f4).endVertex();
        }
//        public static void drawCircle(float x, float y, float start, float end, float radius, float width, boolean filled, Style s) {
//
//            float i;
//            float endOffset;
//            if (start > end) {
//                endOffset = end;
//                end = start;
//                start = endOffset;
//            }
//            GlStateManager.enableBlend();
//            RenderSystem.disableAlphaTest();
//            GL11.glDisable(GL_TEXTURE_2D);
//            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
//            RenderSystem.shadeModel(7425);
//            GL11.glEnable(GL11.GL_LINE_SMOOTH);
//            GL11.glLineWidth(width);
//
//            GL11.glBegin(GL11.GL_LINE_STRIP);
//            for (i = end; i >= start; i--) {
//                ColorUtils.setColor(s.getColor((int) (i * 1)));
//                float cos = (float) (MathHelper.cos((float) (i * Math.PI / 180)) * radius);
//                float sin = (float) (MathHelper.sin((float) (i * Math.PI / 180)) * radius);
//                GL11.glVertex2f(x + cos, y + sin);
//            }
//            GL11.glEnd();
//            GL11.glDisable(GL11.GL_LINE_SMOOTH);
//            if (filled) {
//                GL11.glBegin(GL11.GL_TRIANGLE_FAN);
//                for (i = end; i >= start; i--) {
//                    ColorUtils.setColor(s.getColor((int) (i * 1)));
//                    float cos = (float) MathHelper.cos((float) (i * Math.PI / 180)) * radius;
//                    float sin = (float) MathHelper.sin((float) (i * Math.PI / 180)) * radius;
//                    GL11.glVertex2f(x + cos, y + sin);
//                }
//                GL11.glEnd();
//            }
//
//            RenderSystem.enableAlphaTest();
//            RenderSystem.shadeModel(7424);
//            GL11.glEnable(GL_TEXTURE_2D);
//            GlStateManager.disableBlend();
//        }

        public static void drawCircle1(float x, float y, float start, float end, float radius, float width, boolean filled, int color) {
            float i;
            float endOffset;
            if (start > end) {
                endOffset = end;
                end = start;
                start = endOffset;
            }

            GlStateManager.enableBlend();
            GL11.glDisable(GL_TEXTURE_2D);
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);

            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (i = end; i >= start; i--) {
                ColorUtils.setColor(color);
                float cos = (float) (MathHelper.cos((float) (i * Math.PI / 180)) * radius);
                float sin = (float) (MathHelper.sin((float) (i * Math.PI / 180)) * radius);
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);

            if (filled) {
                GL11.glBegin(GL11.GL_TRIANGLE_FAN);
                for (i = end; i >= start; i--) {
                    ColorUtils.setColor(color);
                    float cos = (float) MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                    float sin = (float) MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                    GL11.glVertex2f(x + cos, y + sin);
                }
                GL11.glEnd();
            }

            GL11.glEnable(GL_TEXTURE_2D);
            GlStateManager.disableBlend();
        }
        public static int loadTexture(BufferedImage image) throws Exception {
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);

            for (int pixel : pixels) {
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
            buffer.flip();

            int textureID = GlStateManager.genTexture();
            RenderSystem.bindTexture(textureID);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
            GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
            RenderSystem.bindTexture(0);
            return textureID;
        }

        public static void drawRoundFace(float x, float y, float width, float height, float radius, float alpha, AbstractClientPlayerEntity target) {
            try {
                ResourceLocation skin = target.getLocationSkin();
                mc.getTextureManager().bindTexture(skin);
                RenderSystem.pushMatrix();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(770, 771);

                float hurtFactor = getHurtPercent(target); // ��������� ������������� �����

                ShaderUtils.FACE_ROUND.attach();
                ShaderUtils.FACE_ROUND.setUniform("location", x * 2, window.getHeight() - height * 2 - y * 2);
                ShaderUtils.FACE_ROUND.setUniform("size", width * 2, height * 2);
                ShaderUtils.FACE_ROUND.setUniform("texture", 0);
                ShaderUtils.FACE_ROUND.setUniform("radius", radius * 2);
                ShaderUtils.FACE_ROUND.setUniform("alpha", alpha);
                ShaderUtils.FACE_ROUND.setUniform("u", (1f / 64) * 8);
                ShaderUtils.FACE_ROUND.setUniform("v", (1f / 64) * 8);
                ShaderUtils.FACE_ROUND.setUniform("w", 1f / 8);
                ShaderUtils.FACE_ROUND.setUniform("h", 1f / 8);
                ShaderUtils.FACE_ROUND.setUniform("hurtFactor", hurtFactor); // �������� �������� � ������

                quadsBegin(x, y, width, height, 7);
                ShaderUtils.FACE_ROUND.detach();
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void drawFace(float x, float y, float width, float height, AbstractClientPlayerEntity target) {
            try {
                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                ResourceLocation skin = target.getLocationSkin();
                mc.getTextureManager().bindTexture(skin);
                float hurtPercent = getHurtPercent(target);
                GL11.glColor4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
                AbstractGui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, width, height, 64, 64);
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static float getRenderHurtTime(LivingEntity hurt) {
            return hurt.hurtTime - (hurt.hurtTime != 0 ? mc.timer.renderPartialTicks : 0);
        }

        public static float getHurtPercent(LivingEntity hurt) {
            return getRenderHurtTime(hurt) / 10f;
        }

        public static void drawRectHorizontalW(
                double x,
                double y,
                double w,
                double h,
                int color,
                int color1) {

            w = x + w;
            h = y + h;

            if (x < w) {
                double i = x;
                x = w;
                w = i;
            }

            if (y < h) {
                double j = y;
                y = h;
                h = j;
            }

            float[] colorOne = ColorUtils.rgba(color);
            float[] colorTwo = ColorUtils.rgba(color1);
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.shadeModel(7425);
            RenderSystem.defaultBlendFunc();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
            bufferbuilder.pos(w, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
            bufferbuilder.pos(w, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
            bufferbuilder.pos(x, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
            bufferbuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferbuilder);
            RenderSystem.shadeModel(7424);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

        public static void drawRectVerticalW(
                double x,
                double y,
                double w,
                double h,
                int color,
                int color1) {

            w = x + w;
            h = y + h;

            if (x < w) {
                double i = x;
                x = w;
                w = i;
            }

            if (y < h) {
                double j = y;
                y = h;
                h = j;
            }

            float[] colorOne = ColorUtils.rgba(color);
            float[] colorTwo = ColorUtils.rgba(color1);
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, h, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
            bufferbuilder.pos(w, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
            bufferbuilder.pos(w, y, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
            bufferbuilder.pos(x, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
            bufferbuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferbuilder);
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
        }

        public static void quadsBegin(float x, float y, float width, float height, int glQuads) {
            buffer.begin(glQuads, DefaultVertexFormats.POSITION_TEX);
            {
                buffer.pos(x, y, 0).tex(0, 0).endVertex();
                buffer.pos(x, y + height, 0).tex(0, 1).endVertex();
                buffer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
                buffer.pos(x + width, y, 0).tex(1, 0).endVertex();
            }
            tessellator.draw();
        }

        public static void quadsBeginC(float x, float y, float width, float height, int glQuads, Vector4i color) {
            buffer.begin(glQuads, DefaultVertexFormats.POSITION_TEX_COLOR);
            {
                buffer.pos(x, y, 0).tex(0, 0).color(color.get(0)).endVertex();
                buffer.pos(x, y + height, 0).tex(0, 1).color(color.get(1)).endVertex();
                buffer.pos(x + width, y + height, 0).tex(1, 1).color(color.get(2)).endVertex();
                buffer.pos(x + width, y, 0).tex(1, 0).color(color.get(3)).endVertex();
            }
            tessellator.draw();
        }

        public static void drawQuads() {
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            {
                buffer.pos(0, 0, 0).tex(0, 1).endVertex();
                buffer.pos(0, Math.max(window.getScaledHeight(), 1), 0).tex(0, 0).endVertex();
                buffer.pos(Math.max(window.getScaledWidth(), 1), Math.max(window.getScaledHeight(), 1), 0).tex(1, 0).endVertex();
                buffer.pos(Math.max(window.getScaledWidth(), 1), 0, 0).tex(1, 1).endVertex();
            }
            tessellator.draw();
        }

        public enum Corner {
            RIGHT, LEFT, TOP_RIGHT, TOP, ALL, DOWN
        }
    }

    public static class Render3D {

        public static void drawBlockBox(BlockPos blockPos, int color) {
            drawBox(new AxisAlignedBB(blockPos).offset(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z), color);
        }

        public static void drawBox(AxisAlignedBB bb, int color) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1);
            float[] rgb = IntColor.rgb(color);
            RenderSystem.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();
            vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();
            RenderSystem.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glPopMatrix();
        }

        public static Vector3d getEntityPosition(Entity entity, float interpolationFactor) {
            double interpolatedX = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, interpolationFactor) - mc.getRenderManager().info.getProjectedView().x;
            double interpolatedY = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, interpolationFactor) - mc.getRenderManager().info.getProjectedView().y;
            double interpolatedZ = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, interpolationFactor) - mc.getRenderManager().info.getProjectedView().z;

            return new Vector3d(interpolatedX, interpolatedY, interpolatedZ);
        }

        public static Vector2d project2D(Vector3d vector) {
            return project2D(vector.x, vector.y, vector.z);
        }

        public static Vector2d project2D(double x, double y, double z) {
            if (mc.getRenderManager().info == null) return new Vector2d();

            Vector3d cameraPosition = mc.getRenderManager().info.getProjectedView();
            Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
            cameraRotation.conjugate();

            Vector3f relativePosition = new Vector3f((float) (cameraPosition.x - x), (float) (cameraPosition.y - y), (float) (cameraPosition.z - z));
            relativePosition.transform(cameraRotation);

            if (mc.gameSettings.viewBobbing) {
                Entity renderViewEntity = mc.getRenderViewEntity();
                if (renderViewEntity instanceof PlayerEntity playerEntity) {
                    float walkedDistance = playerEntity.distanceWalkedModified;
                    float deltaDistance = walkedDistance - playerEntity.prevDistanceWalkedModified;
                    float interpolatedDistance = -(walkedDistance + deltaDistance * mc.getRenderPartialTicks());
                    float cameraYaw = MathHelper.lerp(mc.getRenderPartialTicks(), playerEntity.prevCameraYaw, playerEntity.cameraYaw);

                    Quaternion bobQuaternionX = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(interpolatedDistance * (float) Math.PI - 0.2F) * cameraYaw) * 5, true);
                    bobQuaternionX.conjugate();
                    relativePosition.transform(bobQuaternionX);

                    Quaternion bobQuaternionZ = new Quaternion(Vector3f.ZP, MathHelper.sin(interpolatedDistance * (float) Math.PI) * cameraYaw * 3, true);
                    bobQuaternionZ.conjugate();
                    relativePosition.transform(bobQuaternionZ);

                    Vector3f bobTranslation = new Vector3f(MathHelper.sin(interpolatedDistance * (float) Math.PI) * cameraYaw * 0.5f, -Math.abs(MathHelper.cos(interpolatedDistance * (float) Math.PI) * cameraYaw), 0);
                    bobTranslation.setY(-bobTranslation.getY());
                    relativePosition.add(bobTranslation);
                }
            }

            double fieldOfView = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

            float halfHeight = window.getScaledHeight() / 2f;
            float scaleFactor = halfHeight / (relativePosition.getZ() * (float) Math.tan(Math.toRadians(fieldOfView / 2f)));

            if (relativePosition.getZ() < 0) return new Vector2d(-relativePosition.getX() * scaleFactor + window.getScaledWidth() / 2f, window.getScaledHeight() / 2f - relativePosition.getY() * scaleFactor);

            return null;
        }
    }

    public static class FrameBuffer {

        public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
            return createFrameBuffer(framebuffer, false);
        }

        public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
            if (framebuffer == null || framebuffer.framebufferWidth != window.getFramebufferWidth() || framebuffer.framebufferHeight != window.getFramebufferHeight()) {
                if (framebuffer != null) framebuffer.deleteFramebuffer();

                return new Framebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), depth, Minecraft.IS_RUNNING_ON_MAC);
            }

            return framebuffer;
        }
    }
}