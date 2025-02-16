package ru.novacore.ui.clickgui.settings.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import ru.novacore.NovaCore;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.ui.clickgui.settings.Element;
import ru.novacore.ui.clickgui.settings.FunctionElement;
import ru.novacore.ui.styles.StyleManager;
import ru.novacore.utils.animations.Animation;
import ru.novacore.utils.animations.impl.DecelerateAnimation;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.Vector4i;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.Scissor;
import ru.novacore.utils.render.font.Fonts;

public class BooleanElement extends Element {

    private final Animation animation = new DecelerateAnimation(200, 8);
    public FunctionElement object;
    public BooleanSetting set;
    private float enabledAnim;

    public BooleanElement(FunctionElement element, BooleanSetting set) {
        this.object = element;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.draw(matrixStack, mouseX, mouseY);
        StyleManager styleManager = NovaCore.getInstance().getStyleManager();
        setHeight(18);
        Vector4i vector4i = new Vector4i(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB());
        double max = !set.get() ? 0 : 8;
        enabledAnim = MathUtil.fast(enabledAnim, (float) max, 10);
        RenderUtils.Render2D.drawShadow(x + width - 22, y + 5, 17, 8, 8, vector4i.x, vector4i.y, vector4i.z, vector4i.w);
        RenderUtils.Render2D.drawGradientRound(x + width - 22, y + 5, 17, 8, new Vector4f(3, 3, 3, 3), vector4i.x, vector4i.y, vector4i.z, vector4i.w);
        RenderUtils.Render2D.drawCircle(x + width - 17.5f + enabledAnim, y + 9, 6, ColorUtils.rgba(235, 235, 235, 255));
        Scissor.push();
        Scissor.setFromComponentCoordinates(x + 5, y + 6.5f, (int) (width - 49), 10);
        Fonts.interMedium.drawText(matrixStack, set.getName(), x + 5, y + 6.5f, -1, 6);
        Scissor.unset();
        Scissor.pop();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY, x, y, width, height) && mouseButton == 0) set.set(!set.get());
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }
}