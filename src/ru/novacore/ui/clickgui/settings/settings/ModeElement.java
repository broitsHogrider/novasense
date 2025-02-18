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

        // Отрисовка текста слева
        Fonts.interMedium.drawText(matrixStack, set.getName(), x + 5, y + 12.5f, -1, 6);
        // Отрисовка закруглённого прямоугольника справа, с учетом смещения влево при увеличении ширины
        float rectWidth = Math.max(40, width / 2f);
        RenderUtils.Render2D.drawRound(x + width - rectWidth - 5, y + off, rectWidth, 20 - 6 + offset, 4, ColorUtils.rgb(30, 30, 30));
        Fonts.interMedium.drawText(matrixStack,set.get(), x + width - rectWidth - 2.5f, y + 12.5f, ColorUtils.rgb(220, 220, 220), 6.5f);
        if (opened) RenderUtils.Render2D.drawRectHorizontalW(x + width - rectWidth - 5, y + off + 14, rectWidth, 1, ColorUtils.rgb(36, 36, 36), ColorUtils.rgb(39, 39, 39));

        if (opened) {
            int i = 1;
            for (String s : set.strings) {
                boolean hovered = MathUtil.isHovered(mouseX, mouseY, x, y + off + 20 + i, width, 8);
                animation.put(s, MathUtil.lerp(animation.get(s), hovered ? 2 : 0, 10));
                Fonts.interMedium.drawText(matrixStack, s, x + width - rectWidth - 2.5f + animation.get(s), y + off + 18.5F + i, set.get().equals(s) ? -1 : ColorUtils.rgba(200, 200, 200, 200), 6);
                i += 9;
            }
            height += 3;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float off = 3;
        off += Fonts.interMedium.getHeight(5) / 2f + 2;
        float rectWidth = Math.max(40, width / 2f);
        if (MathUtil.isHovered(mouseX, mouseY, x + width - rectWidth - 5, y + off, 40, 20 - 5)) {
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