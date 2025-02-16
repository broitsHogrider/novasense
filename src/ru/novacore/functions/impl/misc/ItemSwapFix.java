package ru.novacore.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import ru.novacore.NovaCore;
import ru.novacore.events.EventPacket;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;

@FunctionInfo(name = "ItemSwapFix", category = Category.Misc)
public class ItemSwapFix extends Function {

    @Subscribe
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.getPacket() instanceof SHeldItemChangePacket && eventPacket.getType() == EventPacket.Type.RECEIVE) {
            SHeldItemChangePacket packetHeldItemChange = (SHeldItemChangePacket) eventPacket.getPacket();
            if (packetHeldItemChange.getHeldItemHotbarIndex() != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                eventPacket.isCancel();
            }
        }

    }
}
