package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;

import ru.novacore.events.EventPacket;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.ModeSetting;
import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;

@Getter
@FunctionInfo(name = "World", category = Category.Render)
public class World extends Function {

    public ModeSetting time = new ModeSetting("Time", "Day", "Day", "Night");

    public World() {
        addSettings(time);
    }
    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SUpdateTimePacket p) {
            if (time.get().equalsIgnoreCase("Day"))
                p.worldTime = 1000L;
            else
                p.worldTime = 15000L;
        }
    }
}
