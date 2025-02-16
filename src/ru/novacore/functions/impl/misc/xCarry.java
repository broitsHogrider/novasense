package ru.novacore.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import ru.novacore.events.EventPacket;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import net.minecraft.network.play.client.CCloseWindowPacket;
import ru.novacore.functions.api.FunctionInfo;

@FunctionInfo(name = "xCarry", category = Category.Misc)
public class xCarry extends Function {

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null) return;

        if (e.getPacket() instanceof CCloseWindowPacket) {
            e.cancel();
        }
    }
}
