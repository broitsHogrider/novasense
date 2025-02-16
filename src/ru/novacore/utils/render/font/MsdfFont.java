package ru.novacore.utils.render.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Getter;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class MsdfFont {

    @Getter
    private final String name;
    private final Texture texture;
    @Getter
    private final FontData.AtlasData atlas;
    @Getter
    private final FontData.MetricsData metrics;
    private final Map<Integer, MsdfGlyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;
    private boolean filtered = false;

    private MsdfFont(String name, Texture texture, FontData.AtlasData atlas, FontData.MetricsData metrics, Map<Integer, MsdfGlyph> glyphs, Map<Integer, Map<Integer, Float>> kernings) {
        this.name = name;
        this.texture = texture;
        this.atlas = atlas;
        this.metrics = metrics;
        this.glyphs = glyphs;
        this.kernings = kernings;
    }

    public void bind() {
        GlStateManager.bindTexture(this.texture.getGlTextureId());
        if (!this.filtered) {

            this.texture.setBlurMipmapDirect(true, false);
            this.filtered = true;
        }
    }

    public void unbind() {
        GlStateManager.bindTexture(0);
    }

    public void applyGlyphs(Matrix4f matrix, IVertexBuilder processor, float size, String text, float thickness, float x, float y, float z, int red, int green, int blue, int alpha) {
        for (int i = 0; i < text.length(); i++) {
            int currentChar = text.charAt(i);
            MsdfGlyph glyph = this.glyphs.get(currentChar);
            if (glyph == null) continue;

            float kerning = 0.0f;
            if (i > 0) {
                int prevChar = text.charAt(i - 1);
                kerning = this.kernings.getOrDefault(prevChar, Map.of()).getOrDefault(currentChar, 0.0f);
            }

            x += glyph.apply(matrix, processor, size, x + kerning, y, z, red, green, blue, alpha) + thickness;
        }
    }

    public float getWidth(String text, float size) {
        int prevChar = -1;
        float width = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            int _char = (int) text.charAt(i);
            MsdfGlyph glyph = this.glyphs.get(_char);

            if (glyph == null)
                continue;

            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
                width += kerning.getOrDefault(_char, 0.0f) * size;
            }

            width += glyph.getWidth(size);
            prevChar = _char;
        }

        return width;
    }

    public float getWidth(String text, float size, float thickness) {
        float width = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            int _char = text.charAt(i);
            MsdfGlyph glyph = this.glyphs.get(_char);

            if (glyph == null)
                continue;


            width += glyph.getWidth(size) + thickness;
        }

        return width;
    }


    public static MsdfFont.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        public static final String MSDF_PATH = "novacore/fonts/";
        private String name = "undefined";
        private ResourceLocation dataFile;
        private ResourceLocation atlasFile;

        private Builder() {}

        public MsdfFont.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public MsdfFont.Builder withData(String dataFile) {
            this.dataFile = new ResourceLocation(MSDF_PATH + dataFile);
            return this;
        }

        public MsdfFont.Builder withAtlas(String atlasFile) {
            this.atlasFile = new ResourceLocation(MSDF_PATH + atlasFile);
            return this;
        }

        public MsdfFont build() {
            FontData data = IOUtils.fromJsonToInstance(this.dataFile, FontData.class);
            Texture texture = IOUtils.toTexture(this.atlasFile);

            if (data == null)
                throw new RuntimeException("Failed to read font data file: " + this.dataFile.toString());

            float aWidth = data.atlas().width();
            float aHeight = data.atlas().height();
            Map<Integer, MsdfGlyph> glyphs = data.glyphs().stream()
                    .collect(Collectors.toMap(g -> g.unicode(), g -> new MsdfGlyph(g, aWidth, aHeight)));

            Map<Integer, Map<Integer, Float>> kernings = new HashMap<>();
            if (data.kernings() != null) {
                Map<Integer, Map<Integer, Float>> finalKernings = kernings;
                data.kernings().forEach((kerning) -> {
                    Map<Integer, Float> map = finalKernings.computeIfAbsent(kerning.leftChar(), k -> new HashMap<>());
                    map.put(kerning.rightChar(), kerning.advance());
                });
            } else {
                // Игнорируем кернинг, если данных нет
                kernings = Map.of();
            }

            return new MsdfFont(this.name, texture, data.atlas(), data.metrics(), glyphs, kernings);
        }

    }


}