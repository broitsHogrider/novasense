package ru.novacore.ui.clickgui.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import ru.novacore.functions.settings.Setting;

public abstract class Element implements IElement {

    @Getter
    @Setter
    public float x, y, width = 135, height = 15;

    public Setting setting;

    public boolean isHovered(int mouseX, int mouseY, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);

    @Override
    public abstract void mouseReleased(int mouseX, int mouseY, int mouseButton);

    @Override
    public abstract void keyTyped(int keyCode, int scanCode, int modifiers);

    @Override
    public abstract void charTyped(char codePoint, int modifiers);

    @Override
    public void exit() {
    }
}