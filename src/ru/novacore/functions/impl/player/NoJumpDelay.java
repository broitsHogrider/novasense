package ru.novacore.functions.impl.player;

import ru.novacore.events.EventHandler;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;

@FunctionInfo(name = "NoJumpDelay", category = Category.Player)
public class NoJumpDelay extends Function {
    @EventHandler
    public void onUpdate(EventUpdate e) {
        mc.player.jumpTicks = 0;
    }
}
