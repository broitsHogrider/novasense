package ru.novacore.functions.impl.misc;

import ru.novacore.events.EventHandler;
import ru.novacore.events.server.EventPacket;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import net.minecraft.network.play.client.CCloseWindowPacket;
import ru.novacore.functions.api.FunctionInfo;

@FunctionInfo(name = "xCarry", category = Category.Misc)
public class xCarry extends Function {

    @EventHandler
    public void onPacket(EventPacket e) {
        if (mc.player == null) return;

        if (e.getPacket() instanceof CCloseWindowPacket) {
            e.cancel();
        }
    }
}
