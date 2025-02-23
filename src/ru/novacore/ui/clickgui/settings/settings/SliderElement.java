package ru.novacore.ui.clickgui.settings.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import ru.novacore.NovaCore;
import ru.novacore.events.EventSystem;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.ui.clickgui.settings.Element;
import ru.novacore.ui.clickgui.settings.FunctionElement;
import ru.novacore.ui.styles.StyleManager;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.Vector4i;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.Scissor;
import ru.novacore.utils.render.font.Fonts;

public class SliderElement extends Element {

    public FunctionElement object;
    public SliderSetting set;
    private boolean sliding;
    private float animation;
    private float previousValue;

    public SliderElement(FunctionElement element, SliderSetting set) {
        this.object = element;
        this.set = set;
        setting = set;
        this.previousValue = set.get();
    }

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.draw(matrixStack, mouseX, mouseY);
        setHeight(26.5f);
        StyleManager styleManager = NovaCore.getInstance().getStyleManager();
        float sliderWidth = 120 * ((set.get() - set.min) / (set.max - set.min));
        animation = MathUtil.lerp(animation, sliderWidth, 10);
        Vector4i vector4i = new Vector4i(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB());

        RenderUtils.Render2D.drawGradientRound(x + 5, y + 18, 125, 2, new Vector4f(1, 1, 1, 1), ColorUtils.rgba(10, 10, 10, 120), ColorUtils.rgba(10, 10, 10, 120), ColorUtils.rgba(10, 10, 10, 120), ColorUtils.rgba(10, 10, 10, 120));
        RenderUtils.Render2D.drawGradientRound(x + 5, y + 18, animation + 3, 2, new Vector4f(1, 1, 1, 1),vector4i.x, vector4i.y, vector4i.z, vector4i.w);
        RenderUtils.Render2D.drawRoundCircle(MathHelper.clamp(x + 7.5f + animation, x + 0.5f, x + 130.5f), y + 19f, 5, ColorUtils.rgb(230, 230, 230));

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + 5, y + 6.5f, (int) (width - 30 - Fonts.interMedium.getWidth(String.valueOf(set.get()), 6)), 10);
        Fonts.interMedium.drawText(matrixStack, set.getName(), x + 5, y + 6.5f, -1, 6);
        Scissor.unset();
        Scissor.pop();
        Fonts.interMedium.drawText(matrixStack, String.valueOf(set.get()), x + width - 5 - Fonts.interMedium.getWidth(String.valueOf(set.get()), 6), y + 6.5f, -1, 6);

        float clamp = MathHelper.clamp(mouseX - (x + 5), 0, 120);
        if (sliding) {
            float value = clamp / 120 * (set.max - set.min) + set.min;
            value = (float) MathUtil.round(value, set.increment);

            if (value != previousValue) { // Проверяем, изменилось ли значение
                set.set(value);
                previousValue = value; // Обновляем предыдущее значение
            }
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY, x + 5, y + 17, 115, 3) && mouseButton == 0) sliding = true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        sliding = false;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }

    @Override
    public void exit() {
        super.exit();
        sliding = false;
    }
}