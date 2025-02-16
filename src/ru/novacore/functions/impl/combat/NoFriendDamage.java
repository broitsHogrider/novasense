package ru.novacore.functions.impl.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CUseEntityPacket;
import ru.novacore.command.friends.FriendStorage;
import ru.novacore.events.EventPacket;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
// by lapycha and artem
@FunctionInfo(name = "NoFriendDamage", category = Category.Combat)
public class NoFriendDamage extends Function {
    @Subscribe
    public void onEvent(EventPacket event) {
        if (event.getPacket() instanceof CUseEntityPacket) {
            CUseEntityPacket cUseEntityPacket = (CUseEntityPacket) event.getPacket();
            Entity entity = cUseEntityPacket.getEntityFromWorld(mc.world);
            if (entity instanceof RemoteClientPlayerEntity &&
                    FriendStorage.isFriend(entity.getName().getString()) &&
                    cUseEntityPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
                event.cancel();
            }
        }
    }
}
