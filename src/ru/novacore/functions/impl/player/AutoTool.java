package ru.novacore.functions.impl.player;

import ru.novacore.events.EventHandler;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.math.BlockRayTraceResult;
import ru.novacore.functions.api.FunctionInfo;

@FunctionInfo(name = "AutoTool", category = Category.Player)
public class AutoTool extends Function {

    public final BooleanSetting silent = new BooleanSetting("Незаметный", true);

    public int itemIndex = -1, oldSlot = -1;
    boolean status;
    boolean clicked;
    public AutoTool() {
        addSettings(silent);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.player.isCreative()) {
            itemIndex = -1;
            return;
        }

        if (isMousePressed()) {
            itemIndex = findBestToolSlotInHotBar();
            if (itemIndex != -1) {
                status = true;

                if (oldSlot == -1) {
                    oldSlot = mc.player.inventory.currentItem;
                }

                if (silent.get()) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(itemIndex));
                } else {
                    mc.player.inventory.currentItem = itemIndex;
                }
            }
        } else if (status && oldSlot != -1) {
            if (silent.get()) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(oldSlot));
            } else {
                mc.player.inventory.currentItem = oldSlot;
            }

            itemIndex = oldSlot;
            status = false;
            oldSlot = -1;
        }
    }

    @Override
    public void onDisable() {
        status = false;
        itemIndex = -1;
        oldSlot = -1;
        super.onDisable();
    }

    private int findBestToolSlotInHotBar() {
        if (mc.objectMouseOver instanceof BlockRayTraceResult blockRayTraceResult) {
            Block block = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int slot = 0; slot < 9; slot++) {
                float speed = mc.player.inventory.getStackInSlot(slot)
                        .getDestroySpeed(block.getDefaultState());

                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = slot;
                }
            }
            return bestSlot;
        }
        return -1;
    }


    private boolean isMousePressed() {
        return mc.objectMouseOver != null && mc.gameSettings.keyBindAttack.isKeyDown();
    }
}
