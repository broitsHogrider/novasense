package ru.novacore.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import ru.novacore.NovaCore;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.ui.clickgui.settings.Element;
import ru.novacore.ui.clickgui.settings.FunctionElement;
import ru.novacore.ui.clickgui.theme.ThemeDrawing;
import ru.novacore.utils.client.ClientUtil;
import ru.novacore.utils.client.IMinecraft;
import ru.novacore.utils.client.Vec2i;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.Scissor;
import ru.novacore.utils.render.Stencil;
import ru.novacore.utils.render.font.Fonts;
import ru.novacore.utils.text.GradientUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Panel extends Screen implements IMinecraft {
    
    private final ArrayList<FunctionElement> elements = new ArrayList<>();

    private Animation animation = new Animation();

    private Category category = Category.Combat;

    private Vector2f position = new Vector2f(0, 0);
    private Vector2f size = new Vector2f(0, 0);

    public static float scrollingOut;
    public static float scrolling;

    private final ThemeDrawing themeDrawing = new ThemeDrawing();


    public Panel() {
        super(new StringTextComponent("CS-Gui"));
        scrolling = 0;
        for (Function function : NovaCore.getInstance().getFunctionRegistry().getFunctions()) {
            elements.add(new FunctionElement(function));
        }
    }

    @Override
    protected void init() {
        animation = animation.animate(1, 0.25f, Easings.QUINT_OUT);
        super.init();
        size = new Vector2f(400, 270);
        position = new Vector2f(window.scaledWidth() / 2f - size.x / 2f, window.scaledHeight() / 2f - size.y / 2f);
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        Vec2i fixed = ClientUtil.getMouse(mouseX, mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        mc.gameRenderer.setupOverlayRendering(2);
        animation.update();

        if (animation.getValue() < 0.1) {
            closeScreen();
        }
        MathUtil.scaleStart(position.x + (size.x / 2f), position.y + (size.y / 2f), animation.getValue());
        renderPanel(matrixStack, mouseX, mouseY);
        MathUtil.scaleEnd();
        scrollingOut = MathUtil.lerp(scrollingOut, scrolling, 10);
        mc.gameRenderer.setupOverlayRendering();
    }

    private void renderPanel(MatrixStack matrixStack, int mouseX, int mouseY) {

        String clientName = "Novacore";

        RenderUtils.Render2D.drawGradientRound(position.x, position.y, size.x, size.y, new Vector4f(7, 7, 7, 7),
                ColorUtils.rgb(28,28,35),
                ColorUtils.rgb(18,18,25),
                ColorUtils.rgb(18,18,25),
                ColorUtils.rgb(18,18,25));

        RenderUtils.Render2D.drawRectVerticalW(position.x + 100, position.y, 1, size.y, ColorUtils.rgb(31,33,43), ColorUtils.rgb(34,36,46));
        RenderUtils.Render2D.drawRectHorizontalW(position.x + 100, position.y + 30, size.x - 100, 1, ColorUtils.rgb(31,33,43), ColorUtils.rgb(34,36,46));

        Fonts.sfMedium.drawText(matrixStack, category.name() + " -- ", position.x + 115, position.y + 10.5f, -1, 10.5f);
        Fonts.sfMedium.drawText(matrixStack,category.info, position.x + 115 + Fonts.sfMedium.getWidth(category.name() + " -- ", 10.5f), position.y + 10.5f, ColorUtils.rgb(220, 220, 220), 10.5f);

        Fonts.interMedium.drawCenteredText(matrixStack, GradientUtil.gradient(clientName), position.x + 100 / 2f, position.y + 16.5f, 8);

        Fonts.interMedium.drawText(matrixStack, "Main", position.x + 10, position.y + 40, Color.WHITE.getRGB(), 5);
        Fonts.interMedium.drawText(matrixStack, "Other", position.x + 10, position.y + 179, ColorUtils.rgba(200, 200, 200, 150), 5);

        renderCategories(matrixStack, mouseX, mouseY);

        Scissor.push();
        Scissor.setFromComponentCoordinates(position.x + 100, position.y + 35, size.x - 100, size.y - 35);
        drawElements(matrixStack, mouseX, mouseY);
        Scissor.unset();
        Scissor.pop();
    }


    private void renderCategories(MatrixStack matrixStack, int mX, int mY) {
        float offsetY = 25;
        for (Category type : Category.values()) {
            boolean currentCategorySelected = type == category;
            type.anim = MathUtil.lerp(type.anim, currentCategorySelected ? 255 : 0, 10);
            offsetY += type == Category.Theme ? 35 : 25;

            if (type == Category.Theme && currentCategorySelected) {
                Stencil.initStencilToWrite();
                RenderUtils.Render2D.drawRound(position.x + 100, position.y + 5, size.x - 105, size.y - 10, 5, -1);
                Stencil.readStencilBuffer(1);
                themeDrawing.draw(matrixStack, mX, mY, position.x + 100, position.y + 5, size.x, size.y);
                Stencil.uninitStencilBuffer();
            }

            Fonts.interMedium.drawText(matrixStack, type.name(), position.x + 25, position.y + 7 + offsetY, currentCategorySelected ? NovaCore.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB() : -1, 7.5f);
            Fonts.wexside.drawText(matrixStack, type.icon, position.x + 12.5f, position.y + 6.25f + offsetY, currentCategorySelected ? NovaCore.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB() : -1, 10.5f);


            RenderUtils.Render2D.drawGradientRound(position.x, position.y + offsetY + 8, 7, 5, new Vector4f(0, 2, 0, 2), currentCategorySelected ? ColorUtils.getColor(0) : ColorUtils.rgb(40, 40, 40),
                    currentCategorySelected ? ColorUtils.getColor(90) : ColorUtils.rgb(35, 35, 35),
                    currentCategorySelected ? ColorUtils.getColor(180) : ColorUtils.rgb(40, 40, 40),
                    currentCategorySelected ? ColorUtils.getColor(270) : ColorUtils.rgb(35, 35, 35));

            RenderUtils.Render2D.drawShadow(position.x, position.y + offsetY + 8, 7, 5, 10, currentCategorySelected ? ColorUtils.getColor(0) : ColorUtils.rgb(40, 40, 40),
                    currentCategorySelected ? ColorUtils.getColor(90) : ColorUtils.rgb(35, 35, 35),
                    currentCategorySelected ? ColorUtils.getColor(180) : ColorUtils.rgb(40, 40, 40),
                    currentCategorySelected ? ColorUtils.getColor(270) : ColorUtils.rgb(35, 35, 35));
        }
    }

    public void drawElements(MatrixStack matrixStack, int mouseX, int mouseY) {
        Vec2i fixed = ClientUtil.getMouse(mouseX, mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        AtomicInteger index = new AtomicInteger(0);
        Map<Boolean, List<FunctionElement>> partitioned = elements.stream().filter(FunctionElement ->  FunctionElement.module.getCategory() == category).sorted(Comparator.comparing(FunctionElement::getName)).collect(Collectors.partitioningBy(FunctionElement -> index.getAndIncrement() % 2 == 0));

        List<FunctionElement> first = partitioned.get(true);
        List<FunctionElement> second = partitioned.get(false);

        float offset = scrollingOut;
        float sizePanel1 = 0;
        for (FunctionElement FunctionElement : first) {
            FunctionElement.x = position.x + 115;
            FunctionElement.y = position.y + 35 + offset;
            for (Element element : FunctionElement.element) {
                if (element.setting.visible()) FunctionElement.height += element.height;
            }
            if (FunctionElement.y + FunctionElement.height > position.y + 5 && FunctionElement.y < position.y + size.y - 10) FunctionElement.draw(matrixStack, mouseX, mouseY);
            offset += 27 + FunctionElement.getComponentHeight();
            sizePanel1 += 27 + FunctionElement.getComponentHeight();
        }

        offset = scrollingOut;
        float sizePanel2 = 0;
        for (FunctionElement FunctionElement : second) {
            FunctionElement.x = position.x + 255;
            FunctionElement.y = position.y + 35 + offset;
            for (Element element : FunctionElement.element) {
                if (element.setting.visible()) FunctionElement.height += element.height;
            }
            if (FunctionElement.y + FunctionElement.height > position.y + 5 && FunctionElement.y < position.y + size.y - 10) FunctionElement.draw(matrixStack, mouseX, mouseY);
            offset += 27 + FunctionElement.getComponentHeight();
            sizePanel2 += 27 + FunctionElement.getComponentHeight();
        }

        float max = Math.max(sizePanel1 + 35, sizePanel2 + 35);
        if (max < size.y) {
            scrolling = 0;
        } else {
            scrolling = MathHelper.clamp(scrolling, -(max - size.y), 0);
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            animation = animation.animate(0, 0.25f, Easings.QUINT_OUT);
            return false;
        }

        for (FunctionElement FunctionElement : elements) {
            if (FunctionElement.module.getCategory() == category) FunctionElement.keyTyped(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrolling += (float) (delta * 20);
        for (Category type : Category.values()) {
            type.anim = MathUtil.lerp(type.anim, type == category ? 255 : 0, 10);
            if (type == Category.Theme && type == category) {
                themeDrawing.mouseScrolled(mouseX, mouseY, delta);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        for (FunctionElement FunctionElement : elements) {
            FunctionElement.mouseReleased((int) mouseX, (int) mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float offsetY = 25;
        for (Category type : Category.values()) {
            offsetY += 25;
            if (type == Category.Theme && type == category) {
                themeDrawing.click((int) mouseX, (int) mouseY, button);
            }
            if (MathUtil.isHovered((float) mouseX, (float) mouseY, position.x + 10, position.y + offsetY, 90, 30) && button == 0) category = type;
        }

        if (MathUtil.isHovered((float) mouseX, (float) mouseY, position.x + 100, position.y + 5, size.x - 105, size.y - 10)) {
            for (FunctionElement FunctionElement : elements) {
                if (FunctionElement.module.getCategory() == category) FunctionElement.mouseClicked((int) mouseX, (int) mouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
        for (FunctionElement FunctionElement : elements) {
            FunctionElement.exit();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
