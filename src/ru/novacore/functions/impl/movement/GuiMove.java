package ru.novacore.functions.impl.movement;

import ru.novacore.events.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import ru.novacore.events.server.EventPacket;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.player.MoveUtils;

import java.util.ArrayList;
import java.util.List;

@FunctionInfo(name = "GuiMove", category = Category.Movement)
public class GuiMove extends Function {
    public static BooleanSetting safe = new BooleanSetting("Ft обход", false);
    public StopWatch wait = new StopWatch();
    private final List<IPacket<?>> packet = new ArrayList<>();

    public GuiMove() {
        addSettings(safe);
    }

    public void onUpdate() {
        if (mc.player != null) {

            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint};
            if (!wait.hasReached(400)) {
                for (KeyBinding keyBinding : pressedKeys) {
                    keyBinding.setPressed(false);
                }
                return;
            }


            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) {
                return;
            }

            updateKeyBindingState(pressedKeys);
            if(safe.get()) {
                if (!(mc.currentScreen instanceof InventoryScreen) && !packet.isEmpty() && MoveUtils.isMoving()) {
                    new Thread(() -> {
                        wait.reset();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        for (IPacket p : packet) {
                            mc.player.connection.sendPacket(p);
                        }
                        packet.clear();
                    }).start();
                }
            } else {
                KeyBinding[] keys = new KeyBinding[]{mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};
                if (!(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof EditSignScreen)) {
                    KeyBinding[] var2 = keys;
                    int var3 = keys.length;

                    for (int var4 = 0; var4 < var3; ++var4) {
                        KeyBinding keyBinding = var2[var4];
                        keyBinding.setPressed(InputMappings.isKeyDown(window.getHandle(), keyBinding.getDefault().getKeyCode()));
                    }
                }
            }
        }
    }

    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CClickWindowPacket p && MoveUtils.isMoving()) {
            if (mc.currentScreen instanceof InventoryScreen) {
                packet.add(p);
                e.isCancel();
            }
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
    @EventHandler
    public void onUpdate(EventUpdate event) {
        onUpdate();
    }
    @EventHandler
    public void onPacket2(EventPacket event) {
        if (event.getType() == EventPacket.Type.SEND) {
            onPacket(event);
        }
    }
}
