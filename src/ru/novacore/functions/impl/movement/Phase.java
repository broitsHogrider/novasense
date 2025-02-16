package ru.novacore.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;

@FunctionInfo(name = "Phase", category = Category.Movement)
public class Phase extends Function {

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!collisionPredict()) {
            if (mc.gameSettings.keyBindJump.pressed) {
                mc.player.setOnGround(true);
            }
        }
    }

    public boolean collisionPredict() {
        boolean prevCollision = mc.world
                .getCollisionShapes(mc.player, mc.player.getBoundingBox().shrink(0.0625D)).toList().isEmpty();

        return mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().shrink(0.0625D))
                .toList().isEmpty() && prevCollision;
    }
}
