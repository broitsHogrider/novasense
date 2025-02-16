package ru.novacore.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.utils.player.MoveUtils;
// by lapycha and artem
@FunctionInfo(name = "DragonFly", category = Category.Movement)
public class DragonFly extends Function {
    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (event instanceof EventUpdate && DragonFly.mc.player.abilities.isFlying) {
            MoveUtils.setMotion(1.0);
            DragonFly.mc.player.motion.y = 0.0;
            if (DragonFly.mc.gameSettings.keyBindJump.isKeyDown()) {
                DragonFly.mc.player.motion.y = 0.25;
                if (DragonFly.mc.player.moveForward == 0.0f && !DragonFly.mc.gameSettings.keyBindLeft.isKeyDown() && !DragonFly.mc.gameSettings.keyBindRight.isKeyDown()) {
                    DragonFly.mc.player.motion.y = 0.5;
                }
            }
            if (DragonFly.mc.gameSettings.keyBindSneak.isKeyDown()) {
                DragonFly.mc.player.motion.y = -0.25;
                if (DragonFly.mc.player.moveForward == 0.0f && !DragonFly.mc.gameSettings.keyBindLeft.isKeyDown() && !DragonFly.mc.gameSettings.keyBindRight.isKeyDown()) {
                    DragonFly.mc.player.motion.y = -0.5;



                }
            }
        }
    }
}
