package ru.novacore.ui.clickgui.settings;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IElement {

    void draw(MatrixStack matrixStack, int mouseX, int mouseY);

    void mouseClicked(int mouseX, int mouseY, int mouseButton);

    void mouseReleased(int mouseX, int mouseY, int mouseButton);

    void keyTyped(int keyCode, int scanCode, int modifiers);

    void charTyped(char codePoint, int modifiers);

    void exit();
}