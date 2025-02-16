package ru.novacore.ui.altmanager;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.glfw.GLFW;
import ru.novacore.utils.client.ClientUtil;
import ru.novacore.utils.client.IMinecraft;
import ru.novacore.utils.client.Vec2i;
import ru.novacore.utils.font.FontManager;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.Scissor;

import java.util.ArrayList;
import java.util.Iterator;

public class AltManager extends Screen implements IMinecraft {

    public AltManager() {
        super(new StringTextComponent(""));

    }

    public ArrayList<Account> accounts = new ArrayList<>();


    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!altName.isEmpty())
                altName = altName.substring(0, altName.length() - 1);
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!altName.isEmpty())
                accounts.add(new Account(altName));
            typing = false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        altName += Character.toString(codePoint);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float offset = 6f;
        float width = 250f, height = 270f;
        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (RenderUtils.isInRegion(mouseX, mouseY, x + width - offset - 12.5f, y + offset + 31f, FontManager.sfBold[22].getWidth("?"), FontManager.sfBold[22].getFontHeight())) {
            accounts.add(new Account(RandomStringUtils.randomAlphabetic(8)));
            AltConfig.updateFile();
        }
        if (RenderUtils.isInRegion(mouseX, mouseY, x + offset, y + offset + 25f, width - offset * 2f, 20f) && !RenderUtils.isInRegion(mouseX, mouseY, x + width - offset - 12.5f, y + offset + 31f, FontManager.sfBold[22].getWidth("?"), FontManager.sfBold[22].getFontHeight())) {
            typing = !typing;
        }

        // �������� ���������� ����������� ����������� �����/������� ���
        float iter = scrollAn, offsetAccounts = 0f;
        Iterator<Account> iterator = accounts.iterator();
        while (iterator.hasNext()) {
            Account account = iterator.next();

            float scrollY = y + iter * 22f;

            if (RenderUtils.isInRegion(mouseX, mouseY, x + offset + 5f, scrollY + offset + 85f + offsetAccounts, width - offset * 2f - 10f, 20f)) {
                if (button == 0) {
                    mc.session = new Session(account.accountName, "", "", "mojang");
                    ClientUtil.playSound("accountswitch", 55, false);
                }
            }
            if (RenderUtils.isInRegion(mouseX, mouseY, x + offset + width - 30, scrollY + offset + 85f + 8.5f + offsetAccounts, FontManager.relake[16].getWidth("v"), 10)) {
                if (button == 0) {
                    iterator.remove();
                    AltConfig.updateFile();
                }
            }

            iter++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        // ������
        float offset = 6f;
        float width = 250f, height = 270f;
        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (isHovered((int) mouseX, (int) mouseY, x + offset, y + offset + 80f, width - offset * 2f, 177.5f)) scroll += delta * 1;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    public boolean isHovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public float scroll;
    public float scrollAn;

    private String altName = "";
    private boolean typing;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        scrollAn = MathUtil.lerp(scrollAn, scroll, 5);

        mc.gameRenderer.setupOverlayRendering(2);
        RenderUtils.Render2D.drawMainMenuShader(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().scaledHeight());

        float offset = 6f;
        float width = 250f, height = 270f;
        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

// Фон окна
        RenderUtils.Render2D.drawRect(x - offset, y - offset, width + offset * 2f, height + offset * 2f, ColorUtils.rgba(22, 22, 22, 215));

// Заголовок окна
        FontManager.relake[22].drawString(matrixStack, "d", x + offset, y + offset + 1f, -1);
        FontManager.montserrat[22].drawString(matrixStack, "Добро пожаловать!", x + offset + FontManager.relake[16].getWidth("d") + 6f, y + offset, -1);
        FontManager.montserrat[12].drawString(matrixStack, "Рады видеть вас снова!", x + offset, y + offset + 18f, ColorUtils.rgba(180, 180, 180, 255));

// Поле для ввода
        RenderUtils.Render2D.drawRect(x + offset, y + offset + 25f, width - offset * 2f, 20f, ColorUtils.rgba(40, 40, 40, 215));
        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 25f, width - offset * 2f, 20f);
        FontManager.montserrat[15].drawString(matrixStack, typing ? (altName + (typing ? "|" : "")) : "Введите имя аккаунта", x + offset + 2f, y + offset + 32.5f, ColorUtils.rgba(152, 152, 152, 255));
        Scissor.unset();
        Scissor.pop();

// Кнопка подтверждения
        FontManager.relake[22].drawString(matrixStack, "t", x + width - offset - 14.5f, y + offset + 32f, -1);

// Подзаголовок списка аккаунтов
        FontManager.montserrat[22].drawString(matrixStack, "Аккаунты:", x + offset, y + offset + 60f, -1);
        FontManager.montserrat[12].drawString(matrixStack, "Выберите аккаунт из списка!", x + offset, y + offset + 73f, ColorUtils.rgba(180, 180, 180, 255));

// Фон списка аккаунтов
        RenderUtils.Render2D.drawRect(x + offset, y + offset + 80f, width - offset * 2f, 177.5f, ColorUtils.rgba(40, 40, 40, 215));

// Если список аккаунтов пуст
        if (accounts.isEmpty())
            FontManager.montserrat[22].drawCenteredString(matrixStack, "Аккаунты не найдены", x + width / 2f, y + offset + 165.75f, -1);

// Отображение списка аккаунтов
        float size = 0f, iter = scrollAn, offsetAccounts = 0f;
        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 80f, width - offset * 2f, 177.5f);
        for (Account account : accounts) {
            float scrollY = y + iter * 22f;

            RenderUtils.Render2D.drawRect(x + offset + 5f, scrollY + offset + 85f + offsetAccounts, width - offset * 2f - 10f, 20f, account.accountName == mc.session.getUsername() ? ColorUtils.rgba(60, 60, 60, 255) : ColorUtils.rgba(75, 75, 75, 255));

            FontManager.relake[16].drawString(matrixStack, "v", x + offset + width - 30, scrollY + offset + 85f + 8.5f + offsetAccounts, -1);
            FontManager.montserrat[16].drawString(matrixStack, account.accountName, x + offset + 23f, scrollY + offset + 85f + 8f + offsetAccounts, -1);
            FontManager.nurik[16].drawString(matrixStack, "W", x + offset + 9f + 1f, scrollY + offset + 93.5f + offsetAccounts, -1);

            iter++;
            size++;
        }
        scroll = MathHelper.clamp(scroll, size > 8 ? -size + 4 : 0, 0);
        Scissor.unset();
        Scissor.pop();

// Подпись внизу
        FontManager.montserrat[12].drawString(matrixStack, "Ваш текущий аккаунт: " + mc.session.getUsername(), x + offset, y + height - offset / 2, ColorUtils.rgba(180, 180, 180, 255));

        mc.gameRenderer.setupOverlayRendering();

    }


}
