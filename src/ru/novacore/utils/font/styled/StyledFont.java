package ru.novacore.utils.font.styled;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import ru.novacore.utils.font.common.AbstractFont;
import ru.novacore.utils.font.common.Lang;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;

public class StyledFont {

    private final GlyphPage regular;

    public StyledFont(String fileName, int size, float stretching, float spacing, boolean antialiasing, Lang lang) {
        int[] codes = lang.getCharCodes();
        char[] chars = new char[(codes[1] - codes[0] + codes[3] - codes[2])];

        int c = 0;
        for (int d = 0; d <= 2; d += 2) {
            for (int i = codes[d]; i <= codes[d + 1] - 1; i++) {
                chars[c] = (char) i;
                c++;
            }
        }

        this.regular = new GlyphPage(AbstractFont.getFont(fileName, Font.PLAIN, size), chars, stretching, spacing, antialiasing);
    }

    public float renderGlyph(Matrix4f matrix, char c, float x, float y, boolean bold, boolean italic, float red, float green, float blue, float alpha) {
        return getGlyphPage().renderGlyph(matrix, c, x, y, red, green, blue, alpha);
    }

    public void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawShadowedString(matrixStack, this, text, x, y, color);
    }

    public void drawString(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawString(matrixStack, this, text, x, y, color);
    }

    public void drawString(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawString(matrixStack, this, text, x, y, color);
    }

    public void drawStringWithShadow(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawShadowString(matrixStack, this, text, x, y, color);
    }

    public void drawCenteredString(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawCenteredXString(matrixStack, this, text, x, y, color);
    }

    public void drawCenteredString(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawCenteredString(matrixStack, this, text, x, y, color);
    }

    public void drawStringWithOutline(MatrixStack matrixStack, String text, double x, double y, int color) {
        Color c = new Color(0, 0, 0, 128);
        x = MathUtil.round(x, 0.5F);
        y = MathUtil.round(y, 0.5F);
        StyledFontRenderer.drawString(matrixStack, this, text, x - 0.5, y, c.getRGB());
        StyledFontRenderer.drawString(matrixStack, this, text, x + 0.5, y, c.getRGB());
        StyledFontRenderer.drawString(matrixStack, this, text, x, y - 0.5f, c.getRGB());
        StyledFontRenderer.drawString(matrixStack, this, text, x, y + 0.5f, c.getRGB());

        drawString(matrixStack, text, x, y, color);
    }

    public void drawCenteredStringWithOutline(MatrixStack matrixStack, String text, double x, double y, int color) {
        drawStringWithOutline(matrixStack, text, x - getWidth(text) / 2f, y, color);
    }

    public void drawScissorString(MatrixStack matrixStack, String text, double x, double y, int color, int width) {
        StyledFontRenderer.drawScissorString(matrixStack, this, text, x, y, color, width);
    }

    public void drawPrefix(MatrixStack matrixStack, ITextComponent text, float x, float y, int alpha) {
        float offset = 0;
        for (ITextComponent it : text.getSiblings()) {
            for (ITextComponent it1 : it.getSiblings()) {
                String draw = it1.getString();
                if (it1.getStyle().getColor() != null) {
                    drawString(matrixStack, draw, x + offset, y, ColorUtils.setAlpha(ColorUtils.toColor(it1.getStyle().getColor().getHex()), alpha));
                } else {
                    drawString(matrixStack, draw, x + offset, y, ColorUtils.setAlpha(Color.GRAY.getRGB(), alpha));
                }
                offset += getWidth(draw);
            }
            if (it.getSiblings().size() <= 1) {
                String draw = TextFormatting.getTextWithoutFormattingCodes(it.getString());
                drawString(matrixStack, draw, x + offset, y, ColorUtils.setAlpha(it.getStyle().getColor() == null ? Color.GRAY.getRGB() : it.getStyle().getColor().getColor(), alpha));
                offset += getWidth(Objects.requireNonNull(draw));
            }
        }
        if (text.getSiblings().isEmpty()) {
            String draw = TextFormatting.getTextWithoutFormattingCodes(text.getString());
            drawString(matrixStack, draw, x + offset, y, ColorUtils.setAlpha(text.getStyle().getColor() == null ? Color.GRAY.getRGB() : text.getStyle().getColor().getColor(), alpha));
        }
    }

    public float getWidth(String text) {
        float width = 0.0f;

        for (int i = 0; i < text.length(); i++) {
            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length() && StyledFontRenderer.STYLE_CODES.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1)) != -1) {
                i++;
            } else {
                width += getGlyphPage().getWidth(c0) + regular.getSpacing();
            }
        }

        return (width - regular.getSpacing()) / 2f;
    }

    public float getWidth(ITextComponent text) {
        float offset = 0;
        for (ITextComponent it : text.getSiblings()) {
            for (ITextComponent it1 : it.getSiblings()) {
                String draw = it1.getString();
                offset += getWidth(draw);
            }
            if (it.getSiblings().size() <= 1) {
                String draw = TextFormatting.getTextWithoutFormattingCodes(it.getString());
                offset += getWidth(Objects.requireNonNull(draw));
            }
        }
        if (text.getSiblings().isEmpty()) {
            String draw = TextFormatting.getTextWithoutFormattingCodes(text.getString());
            offset += getWidth(Objects.requireNonNull(draw));
        }

        return offset;
    }

    private GlyphPage getGlyphPage() {
        return regular;
    }

    public float getFontHeight() {
        return regular.getFontHeight();
    }
}