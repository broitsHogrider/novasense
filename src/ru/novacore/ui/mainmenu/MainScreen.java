package ru.novacore.ui.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import ru.novacore.NovaCore;
import ru.novacore.utils.client.ClientUtil;
import ru.novacore.utils.client.IMinecraft;
import ru.novacore.utils.client.Vec2i;
import ru.novacore.utils.font.FontManager;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.KawaseBlur;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.Stencil;
import ru.novacore.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class MainScreen extends Screen implements IMinecraft {
    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));

    }

    private final ResourceLocation backmenu = new ResourceLocation("novacore/images/backmenu.png");
    private final ResourceLocation logo = new ResourceLocation("novacore/images/logo.png");

    private final List<Button> buttons = new ArrayList<>();

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        float widthButton = 200;


        for (Particle particle : particles) {
            particle.y = ThreadLocalRandom.current().nextInt(-5, height);
        }
        float x = ClientUtil.calc(width) / 2f - widthButton / 2f;
        float y = Math.round(ClientUtil.calc(height) / 2f - 12);
        buttons.clear();

        buttons.add(new Button(x, y, widthButton, 20, "singleplayer", () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        y += 45 / 2f;
        buttons.add(new Button(x, y, widthButton, 20, "multiplayer", () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));
        y += 45 / 2f;
        buttons.add(new Button(x, y, widthButton, 20, "altmanager", () -> {
            mc.displayGuiScreen(NovaCore.getInstance().getAccount());
        }));
        y += 45 / 2f;
        buttons.add(new Button(x, y, 98, 20, "options", () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));
        buttons.add(new Button(x + 102, y, 98, 20, "exit", mc::shutdownMinecraftApplet));
    }

    private static final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    private final StopWatch stopWatch = new StopWatch();
    static boolean start = false;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (stopWatch.isReached(100)) {
            particles.add(new Particle());
            stopWatch.reset();
        }
        MainWindow mainWindow = mc.getMainWindow();
        int windowWidth = ClientUtil.calc(mainWindow.getScaledWidth());
        int windowHeight = ClientUtil.calc(mainWindow.getScaledHeight());

        int logoWidth = 1920 / 2;
        int logoHeight = 1080 / 2;

        // Расчет координат для рисования логотипа по центру
        int xLogo = (windowWidth - logoWidth) / 2;
        int yLogo = (windowHeight - logoHeight) / 2 + 50;
        // Рисование логотипа по центру
        mc.gameRenderer.setupOverlayRendering(2);

        RenderUtils.Render2D.drawMainMenuShader(windowWidth, windowHeight);

        KawaseBlur.blur.updateBlur(3, 4);
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

    private class Particle {

        private final float x;
        private float y;
        private float size;

        public Particle() {
            x = ThreadLocalRandom.current().nextInt(0, mc.getMainWindow().getScaledWidth());
            y = 0;
            size = 0;
        }

        public void update() {
            y += 1f;
        }

        public void render(MatrixStack stack) {
            //update();
            size += 0.1f;
            GlStateManager.pushMatrix();
            GlStateManager.translated((x + Math.sin((System.nanoTime() / 1000000000f)) * 5), y, 0);
            GlStateManager.rotatef(size * 20, 0, 0, 1);
            GlStateManager.translated(-(x + Math.sin((System.nanoTime() / 1000000000f)) * 5), -y, 0);
            float multi = 1 - MathHelper.clamp((y / mc.getMainWindow().getScaledHeight()), 0, 1);
            y += 1;
            Fonts.interMedium.drawText(stack, "A", (float) (x + Math.sin((System.nanoTime() / 1000000000f)) * 5), y, -1, MathHelper.clamp(size * multi, 0, 9));
            GlStateManager.popMatrix();
            if (y >= mc.getMainWindow().getScaledHeight()) {
                particles.remove(this);
            }
        }

    }

    @AllArgsConstructor
    private class Button {
        @Getter
        private final float x, y, width, height;
        private String text;
        private Runnable action;

        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            int backRoundColor = ColorUtils.rgba(20, 20, 20, 222);
            if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) backRoundColor = ColorUtils.rgba(25, 25, 25, 222);
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
