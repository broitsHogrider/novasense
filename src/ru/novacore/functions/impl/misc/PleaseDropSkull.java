package ru.novacore.functions.impl.misc;

import ru.novacore.events.EventHandler;
import net.minecraft.entity.LivingEntity;
import ru.novacore.NovaCore;
import ru.novacore.events.EventSystem;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.utils.math.StopWatch;

@FunctionInfo(name = "PleaseDropSkull", category = Category.Misc)
public class PleaseDropSkull extends Function {

    LivingEntity entity;

    StopWatch stopWatch = new StopWatch();

    @EventHandler
    public void onUpdate(EventUpdate eventUpdate) {
        if (NovaCore.getInstance().getFunctionRegistry().getAttackAura().getTarget() != null) {
            entity = NovaCore.getInstance().getFunctionRegistry().getAttackAura().getTarget();
        } else {
            entity = null; return;
        }

        if (entity.getHealth() < 4) {
            if (stopWatch.isReached(10000L)) {
                mc.player.sendChatMessage("!" + entity.getName().getString() + " кидай шар/о6/элики/каменьщика/огородника - отпущу, ну пожалуйста");
                stopWatch.reset();
            }
        }
    }

}
