package ru.novacore.ui.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import ru.novacore.NovaCore;
import ru.novacore.events.EventSystem;
import ru.novacore.utils.client.ClientUtil;
import ru.novacore.utils.client.IMinecraft;
import ru.novacore.utils.client.Vec2i;
import ru.novacore.utils.font.FontManager;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;


public class MainScreen extends Screen implements IMinecraft {
    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));
    }

    private final List<Button> buttons = new ArrayList<>();

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        float widthButton = 200;

        float x = ClientUtil.calc(width) / 2f - widthButton / 2f;
        float y = Math.round(ClientUtil.calc(height) / 2f - 12);
        buttons.clear();

        buttons.add(new Button(x, y, widthButton, 20, "Одиночный мир", () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        y += 45 / 2f;
        buttons.add(new Button(x, y, widthButton, 20, "Список серверов", () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));
        y += 45 / 2f;
        buttons.add(new Button(x, y, widthButton, 20, "Аккаунты", () -> {
            mc.displayGuiScreen(NovaCore.getInstance().getAccount());
        }));
        y += 45 / 2f;
        buttons.add(new Button(x, y, 98, 20, "Настройки", () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));
        buttons.add(new Button(x + 102, y, 98, 20, "Выход", mc::shutdownMinecraftApplet));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        MainWindow mainWindow = mc.getMainWindow();
        int windowWidth = ClientUtil.calc(mainWindow.getScaledWidth());
        int windowHeight = ClientUtil.calc(mainWindow.getScaledHeight());

        mc.gameRenderer.setupOverlayRendering(2);

        RenderUtils.Render2D.drawMainMenuShader(windowWidth, windowHeight);
        drawButtons(matrixStack, mouseX, mouseY, partialTicks);
        mc.gameRenderer.setupOverlayRendering();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        buttons.forEach(b -> b.click(fixed.getX(), fixed.getY(), button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawButtons(MatrixStack stack, int mX, int mY, float pt) {

        buttons.forEach(b -> b.render(stack, mX, mY, pt));
    }

    public static final ResourceLocation button = new ResourceLocation("novacore/images/button.png");

    @AllArgsConstructor
    private class Button {
        @Getter
        private final float x, y, width, height;
        private String text;
        private Runnable action;

        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            int backRoundColor = ColorUtils.rgb(16, 16, 16);
            RenderUtils.Render2D.drawRect(x, y, width, height, backRoundColor);

            int color = ColorUtils.rgb(161, 164, 177);
            if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
                color = ColorUtils.rgb(255, 255, 255);
            }
            FontManager.sfBold[18].drawCenteredString(stack, text, x + width / 2f, y + height / 2f - 5.5f + 2, color);

        }

        public void click(int mouseX, int mouseY, int button) {
            if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
                action.run();
            }
        }

    }

}
