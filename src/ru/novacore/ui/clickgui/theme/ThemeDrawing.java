package ru.novacore.ui.clickgui.theme;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;
import ru.novacore.NovaCore;
import ru.novacore.ui.clickgui.ThemePanel;
import ru.novacore.ui.styles.Style;
import ru.novacore.utils.math.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class ThemeDrawing {
    private final List<ThemePanel> objects = new ArrayList<>();
    private float scrollOffset = 0;
    private float maxScroll;
    private float targetScrollOffset = 0;

    boolean colorOpen;
    public float openAnimation;

    float x, y, width, height;

    public ThemeDrawing() {
        for (Style style : NovaCore.getInstance().getStyleManager().getStyleList()) {
            objects.add(new ThemePanel(style));
        }
    }

    public void draw(MatrixStack stack, int mouseX, int mouseY, float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        scrollOffset = MathUtil.lerp(scrollOffset, targetScrollOffset, 10); // Плавная интерполяция
        openAnimation = MathUtil.lerp(openAnimation, colorOpen ? 1 : 0, 10);

        float offsetY = y + 30 - scrollOffset;

        // Высота для всех панелей
        float totalContentHeight = 0;
        for (ThemePanel object : objects) {
            object.x = x + 5;
            object.y = offsetY;
            object.width = 235;
            object.height = 20;

            offsetY += object.height + 5;
            totalContentHeight += object.height + 5;
        }

        // Рассчитываем максимальный скролл
        maxScroll = Math.max(0, totalContentHeight - height);

        // Отрисовка всех панелей
        for (ThemePanel object : objects) {
            if (object.y + object.height >= y && object.y <= y + height) { // Оптимизация отображения видимых объектов
                object.draw(stack, mouseX, mouseY);
            }
        }
    }

    public void click(int mouseX, int mouseY, int button) {
        for (ThemePanel object : objects) {
            if (MathUtil.isHovered(mouseX, mouseY, object.x, object.y, object.width, object.height)) {
                NovaCore.getInstance().getStyleManager().setCurrentStyle(object.style);
            }
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        targetScrollOffset = MathHelper.clamp(targetScrollOffset - (float) delta * 20, 0, maxScroll);
        return true;
    }
}
