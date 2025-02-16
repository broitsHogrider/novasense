package ru.novacore.utils.font.common;

import lombok.Getter;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import ru.novacore.utils.font.FontManager;
import ru.novacore.utils.font.TextureHelper;
import ru.novacore.utils.font.Wrapper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractFont implements Wrapper {

    protected Map<Character, Glyph> glyphs = new HashMap<>();
    protected int texId, imgWidth, imgHeight;
    @Getter
    protected float fontHeight;
    protected String fontName;
    protected boolean antialiasing;

    public static Font getFont(String fileName, int style, int size) {
        String path = FontManager.FONT_DIR.concat(fileName);
        Font font = null;

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(FontManager.class.getResourceAsStream(path))).deriveFont(style, size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        return font;
    }

    public static Font getFontWindows(String fileName, int style, int size) {
        Font font = null;

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, Files.newInputStream(new File("C:/Windows/Fonts/" + fileName).toPath())).deriveFont(style, size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        return font;
    }

    public Graphics2D setupGraphics(BufferedImage img, Font font) {
        Graphics2D graphics = img.createGraphics();

        graphics.setFont(font);
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, imgWidth, imgHeight);
        graphics.setColor(Color.WHITE);

        if (antialiasing) {
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        return graphics;
    }

    public float renderGlyph(Matrix4f matrix, char c, float x, float y, float red, float green, float blue, float alpha) {
        Glyph glyph = glyphs.get(c);
        if (glyph == null)
            return 0;
        float pageX = glyph.x / (float) imgWidth;
        float pageY = glyph.y / (float) imgHeight;
        float pageWidth = glyph.width / (float) imgWidth;
        float pageHeight = glyph.height / (float) imgHeight;
        float width = glyph.width + getStretching();
        float height = glyph.height;

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        buffer.pos(matrix, x, y + height, 0).color(red, green, blue, alpha)
                .tex(pageX, pageY + pageHeight).endVertex();
        buffer.pos(matrix, x + width, y + height, 0).color(red, green, blue, alpha)
                .tex(pageX + pageWidth, pageY + pageHeight).endVertex();
        buffer.pos(matrix, x + width, y, 0).color(red, green, blue, alpha)
                .tex(pageX + pageWidth, pageY).endVertex();
        buffer.pos(matrix, x, y, 0).color(red, green, blue, alpha)
                .tex(pageX, pageY).endVertex();
        tessellator.draw();

        return width + getSpacing();
    }

    public float getWidth(char ch) {
        Glyph glyph = glyphs.get(ch);
        return (glyph != null) ? (glyph.width + getStretching()) : 0;
    }

    public float getWidth(String text) {
        if (text == null) return 0;

        float width = 0;
        float sp = getSpacing();

        for (int i = 0; i < text.length(); i++) {
            width += getWidth(text.charAt(i)) + sp;
        }

        return (width - sp) / 2f;
    }

    public abstract float getStretching();

    public abstract float getSpacing();

    protected final void setTexture(BufferedImage img) {
        texId = TextureHelper.loadTexture(img);
    }

    public static class Glyph {
        public int x;
        public int y;
        public int width;
        public int height;
    }
}