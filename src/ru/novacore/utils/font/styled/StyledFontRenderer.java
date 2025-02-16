package ru.novacore.utils.font.styled;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import ru.novacore.utils.font.Wrapper;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.ShaderUtils;

import java.awt.*;

public class StyledFontRenderer implements Wrapper {

    public static final String STYLE_CODES = "0123456789abcdefklmnor";
    public static final int[] COLOR_CODES = new int[32];

    static {
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            COLOR_CODES[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    public static float drawString(MatrixStack matrixStack, StyledFont font, String text, double x, double y, int color) {
        return renderString(matrixStack, font, text, x, y, false, color);
    }

    public static float drawString(MatrixStack matrixStack, StyledFont font, ITextComponent text, double x, double y, int color) {
        return renderString(matrixStack, font, text, x, y, false, color);
    }

    public static float drawScissorString(MatrixStack matrixStack, StyledFont font, String text, double x, double y, int color, int width) {
        return renderScissorString(matrixStack, font, text, x, y, false, color, width);
    }

    public static void drawShadowedString(MatrixStack matrixStack, StyledFont font, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawShadowITextComponentString(matrixStack, font, text, x, y, color);
    }

    public static void drawShadowITextComponentString(MatrixStack matrixStack, StyledFont font, ITextComponent text, double x, double y, int color) {
        renderString(matrixStack, font, text, x + 1, y, true, color);
        renderString(matrixStack, font, text, x, y - 1, false, color);
    }

    public static void drawCenteredXString(MatrixStack matrixStack, StyledFont font, String text, double x, double y, int color) {
        renderString(matrixStack, font, text, x - font.getWidth(text) / 2f, y, false, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, StyledFont font, ITextComponent text, double x, double y, int color) {
        renderString(matrixStack, font, text, x - font.getWidth(text.getString()) / 2f, y, false, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, StyledFont font, String text, double x, double y, int color) {
        renderString(matrixStack, font, text, x - font.getWidth(text) / 2f, y, false, color);
    }

    public static void drawShadowString(MatrixStack matrixStack, StyledFont font, String text, double x, double y, int color) {
        renderStringWithShadow(matrixStack, font, text, x, y, color, getShadowColor(color));
    }

    private static void renderStringWithShadow(MatrixStack matrixStack, StyledFont font, String text, double x, double y, int color, int shadowColor) {
        renderString(matrixStack, font, text, x + 1, y, true, shadowColor);
        renderString(matrixStack, font, text, x, y - 1, false, color);
    }

    private static float renderString(MatrixStack matrixStack, StyledFont font, String text, double x, double y, boolean shadow, int color) {
        y -= 3;
        GL11.glColor4f(1, 1, 1, 1);

        float startPos = (float) x * 2.0f;
        float posX = startPos;
        float posY = (float) y * 2.0f;

        float[] rgb = RenderUtils.IntColor.rgb(color);
        float red = rgb[0];
        float green = rgb[1];
        float blue = rgb[2];
        float alpha = rgb[3];


        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        matrixStack.push();
        matrixStack.scale(0.5f, 0.5f, 0.5f);

        Matrix4f matrix = matrixStack.getLast().getMatrix();
        int length = text.length();
        String lowerCaseText = text.toLowerCase();

        for (int i = 0; i < length; i++) {
            char c0 = text.charAt(i);

            if (c0 == 167 && i + 1 < length && STYLE_CODES.indexOf(lowerCaseText.charAt(i + 1)) != -1) {
                int i1 = STYLE_CODES.indexOf(lowerCaseText.charAt(i + 1));

                if (i1 < 16) {
                    if (shadow) {
                        i1 += 16;
                    }

                    int j1 = COLOR_CODES[i1];

                    red = (float) (j1 >> 16 & 255) / 255.0F;
                    green = (float) (j1 >> 8 & 255) / 255.0F;
                    blue = (float) (j1 & 255) / 255.0F;
                }
                i++;
            } else {
                posX += font.renderGlyph(matrix, c0, posX, posY, false, false, red, green, blue, alpha);
            }
        }

        matrixStack.pop();
        GlStateManager.disableBlend();

        return (posX - startPos) / 2.0f;
    }

    private static float renderString(MatrixStack matrixStack, StyledFont font, ITextComponent text, double x, double y, boolean shadow, int color) {
        y -= 3;
        GL11.glColor4f(1, 1, 1, 1);

        float startPos = (float) x * 2.0f;
        float posX = startPos;
        float posY = (float) y * 2.0f;
        float red = 1;
        float green = 1;
        float blue = 1;
        float alpha = 1;
        boolean bold = false;
        boolean italic = false;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        matrixStack.push();
        matrixStack.scale(0.5f, 0.5f, 1f);

        Matrix4f matrix = matrixStack.getLast().getMatrix();

        for (int i = 0; i < text.getString().length(); i++) {
            if (i >= text.getSiblings().size()) {
                char c0 = text.getString().charAt(i);

                if (c0 == 167 && i + 1 < text.getString().length() && STYLE_CODES.indexOf(text.getString().toLowerCase().charAt(i + 1)) != -1) {
                    int i1 = STYLE_CODES.indexOf(text.getString().toLowerCase().charAt(i + 1));

                    if (i1 < 16) {
                        if (shadow) {
                            i1 += 16;
                        }

                        int j1 = COLOR_CODES[i1];

                        red = (float) (j1 >> 16 & 255) / 255.0F;
                        green = (float) (j1 >> 8 & 255) / 255.0F;
                        blue = (float) (j1 & 255) / 255.0F;
                    }
                    i++;
                } else {
                    posX += font.renderGlyph(matrix, c0, posX, posY, false, false, red, green, blue, alpha);
                }
                continue;
            }
            ITextComponent c = text.getSiblings().get(i);
            if (c.getString().isEmpty()) {
                char c0 = text.getString().charAt(i);

                if (c0 == 167 && i + 1 < text.getString().length() && STYLE_CODES.indexOf(text.getString().toLowerCase().charAt(i + 1)) != -1) {
                    int i1 = STYLE_CODES.indexOf(text.getString().toLowerCase().charAt(i + 1));

                    if (i1 < 16) {
                        if (shadow) {
                            i1 += 16;
                        }

                        int j1 = COLOR_CODES[i1];

                        red = (float) (j1 >> 16 & 255) / 255.0F;
                        green = (float) (j1 >> 8 & 255) / 255.0F;
                        blue = (float) (j1 & 255) / 255.0F;
                    }
                    i++;
                } else {
                    posX += font.renderGlyph(matrix, c0, posX, posY, false, false, red, green, blue, alpha);
                }
                continue;
            }
            char c0 = c.getString().charAt(0);

            if (c.getStyle().getColor() != null) {
                int col = c.getStyle().getColor().getColor();
                red = (float) (col >> 16 & 255) / 255.0F;
                green = (float) (col >> 8 & 255) / 255.0F;
                blue = (float) (col & 255) / 255.0F;
            }

            float f = font.renderGlyph(matrix, c0, posX, posY, bold, italic, red, green, blue, alpha);
            posX += f;

        }


        matrixStack.pop();
        GlStateManager.disableBlend();

        return (posX - startPos) / 2.0f;
    }

    private static float renderScissorString(MatrixStack matrixStack, StyledFont font, String text, double x, double y, boolean shadow, int color, int width) {
        y -= 3;

        float startPos = (float) x * 2;
        float posX = startPos;
        float posY = (float) y * 2;

        float[] rgb = RenderUtils.IntColor.rgb(color);
        float red = rgb[0];
        float green = rgb[1];
        float blue = rgb[2];
        float alpha = rgb[3];

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 771);
        ShaderUtils.TEXT_MASK.attach();
        ShaderUtils.TEXT_MASK.setUniform("inColor", red, green, blue, alpha);
        ShaderUtils.TEXT_MASK.setUniform("width", (float) width);
        ShaderUtils.TEXT_MASK.setUniform("maxWidth", (float) (x + width) * 2);
        matrixStack.push();
        matrixStack.scale(0.5f, 0.5f, 1);

        Matrix4f matrix = matrixStack.getLast().getMatrix();
        int length = text.length();
        String lowerCaseText = text.toLowerCase();

        for (int i = 0; i < length; i++) {
            char c0 = text.charAt(i);

            if (c0 == 167 && i + 1 < length && STYLE_CODES.indexOf(lowerCaseText.charAt(i + 1)) != -1) {
                int i1 = STYLE_CODES.indexOf(lowerCaseText.charAt(i + 1));

                if (i1 < 16) {
                    if (shadow) {
                        i1 += 16;
                    }

                    int j1 = COLOR_CODES[i1];

                    red = (float) (j1 >> 16 & 255) / 255f;
                    green = (float) (j1 >> 8 & 255) / 255f;
                    blue = (float) (j1 & 255) / 255f;
                }
                i++;
            } else {
                posX += font.renderGlyph(matrix, c0, posX, posY,false, false, red, green, blue, alpha);
            }
        }

        matrixStack.pop();
        ShaderUtils.TEXT_MASK.detach();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();

        return (posX - startPos) / 2f;
    }

    public static int getShadowColor(int color) {
        return new Color((color & 16579836) >> 2 | color & -16777216).getRGB();
    }
}