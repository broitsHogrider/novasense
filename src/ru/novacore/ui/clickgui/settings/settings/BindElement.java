package ru.novacore.ui.clickgui.settings.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import ru.novacore.functions.settings.impl.BindSetting;
import ru.novacore.ui.clickgui.settings.Element;
import ru.novacore.ui.clickgui.settings.FunctionElement;
import ru.novacore.utils.client.KeyStorage;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.Scissor;
import ru.novacore.utils.render.font.Fonts;

public class BindElement extends Element {

    public FunctionElement object;
    public BindSetting set;

    public BindElement(FunctionElement element, BindSetting set) {
        this.object = element;
        this.set = set;
        setting = set;
    }

    private boolean binding = false;

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.draw(matrixStack, mouseX, mouseY);
        setHeight(18);
        float max = Math.max(11, Fonts.interMedium.getWidth(KeyStorage.getKey(set.get()) == null ? "none" : KeyStorage.getKey(set.get()).toUpperCase(), 5) + 6);
        RenderUtils.Render2D.drawRound(x + width - max - 5, y + 5, max, 8, 2.5f, ColorUtils.rgb(27, 27, 27));
        if (binding) {
            Fonts.interMedium.drawCenteredText(matrixStack, System.currentTimeMillis() % 1000 > 500 ? "_" : "", x + width - max - 5 + max / 2f, y + 7, -1, 5);
        } else {
            Fonts.interMedium.drawCenteredText(matrixStack, KeyStorage.getKey(set.get()) == null ? "none" : KeyStorage.getKey(set.get()).toUpperCase(), x + width - max - 5 + max / 2f, y + 7, -1, 5);
        }
        Scissor.push();
        Scissor.setFromComponentCoordinates(x + 5, y + 6.5, (int) (width - max - 25), Fonts.interMedium.getHeight(6));
        Fonts.interMedium.drawText(matrixStack, set.getName(), x + 5, y + 6.5f, -1, 6);
        Scissor.unset();
        Scissor.pop();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY) && mouseButton == 0) {
            binding = !binding;
        }

        if (binding && mouseButton >= 1) {
            System.out.println(-100 + mouseButton);
            set.set(-100 + mouseButton);
            binding = false;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                set.set(-1);
                binding = false;
                return;
            }
            set.set(keyCode);
            binding = false;
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }
}