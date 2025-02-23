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
        int selectedCount = 0;
        for (BooleanSetting setting : list) {
            if (setting.get()) {
                selectedCount++;
            }
        }

        Fonts.interMedium.drawText(matrixStack, set.getName(), x + 5, y + 12.5f, -1, 6);

        float rectWidth = Math.max(40, width / 2f);
        RenderUtils.Render2D.drawRound(x + width - rectWidth - 5, y + 10, rectWidth, open ? (list.size() * 13 + 17.5f) : 13, 4, ColorUtils.rgb(30, 30, 30));

        Fonts.interMedium.drawText(matrixStack,"Selected " + selectedCount + "/" + list.size(), x + width - rectWidth - 2.5f, y + 13.5f, ColorUtils.rgb(220, 220, 220), 6.5f);

        int i = 0;
        int offset = 0;
        if (open) {
            RenderUtils.Render2D.drawRectHorizontalW(x + width - rectWidth - 5, y + 10 + 14, rectWidth, 1, ColorUtils.rgb(36, 36, 36), ColorUtils.rgb(39, 39, 39));
            for (BooleanSetting mode : list) {
                offset += 13;
                Scissor.push();
                Scissor.setFromComponentCoordinates(x + width - rectWidth - 5, y + 14.5f + (float) offset, rectWidth, 10);
                boolean hover = MathUtil.isHovered(mouseX, mouseY, x + 9, y + 14.5f + offset, width, 10);
                anim.put(mode, MathUtil.lerp(anim.get(mode), hover ? 2 : 0, 10));
                Fonts.interMedium.drawText(matrixStack, mode.getName(), x + width - rectWidth - 2.5f + anim.get(mode), y + 14.5f + (float) offset, mode.get() ? -1 : ColorUtils.rgba(200, 200, 200, 200), 6);
                Scissor.unset();
                Scissor.pop();
                i++;
            }
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float off = 3;
        off += Fonts.interMedium.getHeight(5) / 2f + 2;
        float rectWidth = Math.max(40, width / 2f);
        if (MathUtil.isHovered(mouseX, mouseY, x + width - rectWidth - 5, y + off, 40, 20 - 5)) {
            open = !open;
        }

        if (!open) return;
        int i = 0;
        int offset = 0;
        for (BooleanSetting s : set.get()) {
            offset += 13;
            if (MathUtil.isHovered(mouseX, mouseY, x + width - rectWidth - 2.5f,y + 14.5f + (float) offset, Fonts.interMedium.getWidth(set.getName(), 6), Fonts.interMedium.getHeight(6))) s.set(!s.get());
            i++;
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