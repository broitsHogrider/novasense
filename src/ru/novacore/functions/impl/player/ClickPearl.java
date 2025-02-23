package ru.novacore.functions.impl.player;

import ru.novacore.events.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import ru.novacore.events.input.EventKey;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BindSetting;
import ru.novacore.utils.player.InventoryUtil;

@FunctionInfo(name = "ClickPearl", category = Category.Player)
public class ClickPearl extends Function {

    private final BindSetting bindSetting = new BindSetting("Кнопка", -1);

    public ClickPearl() {
        addSettings(bindSetting);
    }

    @EventHandler
    private void onKey(EventKey eventKey) {
        if (eventKey.getKey() == bindSetting.get() && !mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
            int hotbarPearls = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
            if (hotbarPearls != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotbarPearls));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            } else {
                int invPearls = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, false);
                if (invPearls != -1) {
                    mc.playerController.pickItem(invPearls);
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    mc.playerController.pickItem(invPearls);
                }
            }
        }
    }
}
