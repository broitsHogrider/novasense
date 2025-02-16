package ru.novacore.ui.clickgui.settings.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.ui.clickgui.settings.Element;
import ru.novacore.ui.clickgui.settings.FunctionElement;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.font.Fonts;
import ru.novacore.utils.text.GradientUtil;

import java.util.HashMap;

public class ModeElement extends Element {

    public FunctionElement object;
    public ModeSetting set;
    public HashMap<String, Float> animation = new HashMap<>();
    private boolean opened;
    
    public ModeElement(FunctionElement element, ModeSetting set) {
        this.object = element;
        this.set = set;
        for (String s : set.strings) {
            animation.put(s, 0f);
        }
        setting = set;
    }

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.draw(matrixStack, mouseX, mouseY);
        float off = 4;
        float offset = 17 - 12;
        for (String s : set.strings) {
            offset += 9;
        }
        if (!opened) offset = 0;
        off += Fonts.interMedium.getHeight(5) / 2f + 2;
        setHeight(offset + 27);
        RenderUtils.Render2D.drawRound(x + 5, y + off, width - 10, 20 - 6 + offset, 4, ColorUtils.rgba(20, 20, 20, 100));
//        Fonts.interMedium.drawText(matrixStack, set.get(), x + 10, y + 12.5f, -1, 6);
        if (opened) {
            int i = 1;
            for (String s : set.strings) {
                boolean hovered = MathUtil.isHovered(mouseX, mouseY, x, y + off + 20 + i, width, 8);
                animation.put(s, MathUtil.lerp(animation.get(s), hovered ? 2 : 0, 10));
                Fonts.interMedium.drawText(matrixStack, s, x + 9 + animation.get(s), y + off + 18.5F + i, set.get().equals(s) ? -1 : ColorUtils.rgba(200, 200, 200, 200), 6);
                i += 9;
            }
            height += 3;
        }
        if (!MathUtil.isHovered(mouseX, mouseY, x+ 5, y + off, width - 10, 20 - 6 + offset)) {
            Fonts.interMedium.drawCenteredText(matrixStack, GradientUtil.gradient(set.getName()), x + width / 2f, y + 12.5f, 6);
        } else {
            Fonts.interMedium.drawCenteredText(matrixStack, set.get(), x + width / 2f, y + 12.5f, -1,6);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float off = 3;
        off += Fonts.interMedium.getHeight(5) / 2f + 2;
        if (MathUtil.isHovered(mouseX, mouseY, x + 5, y + off, width - 10, 20 - 5)) {
            opened = !opened;
        }


        if (!opened) return;
        int i = 1;
        for (String s : set.strings) {
            if (MathUtil.isHovered(mouseX, mouseY, x, y + off + 20F + i, width, 8))
                set.set(s);
            i += 9;
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