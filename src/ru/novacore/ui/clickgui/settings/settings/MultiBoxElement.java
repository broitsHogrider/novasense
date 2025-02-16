package ru.novacore.ui.clickgui.settings.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.ui.clickgui.settings.Element;
import ru.novacore.ui.clickgui.settings.FunctionElement;
import ru.novacore.utils.animations.Animation;
import ru.novacore.utils.animations.Direction;
import ru.novacore.utils.animations.impl.DecelerateAnimation;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.Scissor;
import ru.novacore.utils.render.font.Fonts;
import ru.novacore.utils.text.GradientUtil;

import java.util.HashMap;
import java.util.List;

public class MultiBoxElement extends Element {

    private final Animation animation = new DecelerateAnimation(100, 29);
    public FunctionElement object;
    public ModeListSetting set;
    private boolean open;
    public HashMap<BooleanSetting, Float> anim = new HashMap<>();
    public MultiBoxElement(FunctionElement element, ModeListSetting set) {
        this.object = element;
        this.set = set;
        for (BooleanSetting s : set.get()) {
            anim.put(s, 0f);
        }
        setting = set;
    }

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.draw(matrixStack, mouseX, mouseY);

        List<BooleanSetting> list = set.get();
        animation.setEndPoint(13 * list.size());
        animation.setDirection(open ? Direction.FORWARDS : Direction.BACKWARDS);
        setHeight((float) (animation.getOutput() + (open ? 29.5f : 25)));

        //DisplayUtils.drawRoundedRect(x + 5, y + 6, width - 10, 13, 5.5f, ColorUtils.rgba(20, 20, 20, 100), open ? DisplayUtils.Corner.TOP : DisplayUtils.Corner.ALL);
        RenderUtils.Render2D.drawRound(x + 5, y + 6, width - 10, open ? (list.size() * 13 + 17.5f) : 13, 5.5f, ColorUtils.rgba(20, 20, 20, 100));

        int i = 0;
        int offset = 0;
        if (open) {
            for (BooleanSetting mode : list) {
                offset += 13;
                Scissor.push();
                Scissor.setFromComponentCoordinates(x + 9, y + 14.5f + (float) offset, width - 10, 10);
                boolean hover = MathUtil.isHovered(mouseX, mouseY, x + 9, y + 14.5f + offset, width, 10);
                anim.put(mode, MathUtil.lerp(anim.get(mode), hover ? 2 : 0, 10));
                Fonts.interMedium.drawText(matrixStack, mode.getName(), x + 9 + anim.get(mode), y + 14.5f + (float) offset, mode.get() ? -1 : ColorUtils.rgba(200, 200, 200, 200), 6);
                Scissor.unset();
                Scissor.pop();
                i++;
            }
        }

        Fonts.interMedium.drawCenteredText(matrixStack, GradientUtil.gradient(set.getName()), x + width / 2f, y + 10, 6);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MathUtil.isHovered(mouseX, mouseY, x + 5, y + 6, width - 10, 13) && mouseButton != 2) open = !open;

        int i = 0;
        int offset = 0;
        if (open) {
            for (BooleanSetting ignored : set.get()) {
                offset += 13;
                if (MathUtil.isHovered(mouseX, mouseY, x + 9, y + 14.0f + offset, width - 10, 13) && mouseButton != 2) {
                    System.out.println("Клик по элементу: " + ignored.getName());
                    ignored.set(!ignored.get());
                }
                i++;
            }
        }
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