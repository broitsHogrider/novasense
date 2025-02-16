package ru.novacore.ui.clickgui.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.settings.Setting;
import ru.novacore.functions.settings.impl.*;
import ru.novacore.ui.clickgui.settings.settings.*;
import ru.novacore.utils.animations.Animation;
import ru.novacore.utils.animations.Direction;
import ru.novacore.utils.animations.impl.DecelerateAnimation;
import ru.novacore.utils.client.KeyStorage;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.font.Fonts;

import java.util.ArrayList;

public class FunctionElement extends Element {

    private final Animation animation = new DecelerateAnimation(300, 255);
    public ArrayList<Element> element = new ArrayList<>();
    public boolean binding = false;
    public Function module;
    public BindSetting set;

    public FunctionElement(Function module) {
        this.module = module;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting option) {
                element.add(new BooleanElement(this, option));
            }
            if (setting instanceof SliderSetting option) {
                element.add(new SliderElement(this, option));
            }
            if (setting instanceof ModeSetting option) {
                element.add(new ModeElement(this, option));
            }
//            if (setting instanceof ColorSetting option) {
//                element.add(new ColorPickerElement(this, option));
//            }
            if (setting instanceof ModeListSetting option) {
                element.add(new MultiBoxElement(this, option));
            }
            if (setting instanceof BindSetting option) {
                element.add(new BindElement(this, option));
            }
        }
    }

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.draw(matrixStack, mouseX, mouseY);
        setHeight(22);

        animation.setDirection(module.isState() ? Direction.FORWARDS : Direction.BACKWARDS);

        RenderUtils.Render2D.drawRound(x, y, width, height + getComponentHeight(), 5, ColorUtils.rgb(16, 16, 16));

        float offset = y + height;
        for (Element element : element) {
            if (element.setting.visible()) {
                element.x = x;
                element.y = offset;
                element.draw(matrixStack, mouseX, mouseY);
                offset += element.height;
            }
        }
        int bind = module.getBind();
        String key = KeyStorage.getKey(bind);

        if (key == null) {
            key = "...";
        }

        if (module.isState()) {
            RenderUtils.Render2D.drawShadow(x + 5, y + 7.5f, Fonts.interMedium.getWidth(binding ? "Модуль забинжен: [" + key.toUpperCase() + "]" : module.getName(), 6.5f), 5, 10, ColorUtils.rgba(240, 240, 240, 100));
        }

        Fonts.interMedium.drawText(matrixStack, binding ? "Модуль забинжен: " + module.getBind() : module.getName(), x + 5, y + 8f,  module.isState() ? -1 : ColorUtils.rgba(200, 200, 200, 200), 6.5f);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (Element element : element) {
            element.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (isHovered(mouseX, mouseY, 20)) {
            if (mouseButton == 0) module.toggle();
            if (mouseButton == 2) binding = !binding;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (Element element : element) {
            element.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        for (Element element : element) {
            element.keyTyped(keyCode, scanCode, modifiers);
        }
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                module.bind = 0;
                binding = false;
                return;
            }
            module.bind = keyCode;
            binding = false;
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Element element : element) {
            element.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void exit() {
        super.exit();
        for (Element element : element) {
            element.exit();
        }
    }

    public float getComponentHeight() {
        float height = 0;
        for (Element element : element) {
            if (element.setting.visible()) height += element.getHeight();
        }
        return height;
    }

    public String getName() {
        return module.getName();
    }
}