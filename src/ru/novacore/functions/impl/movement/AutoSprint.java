package ru.novacore.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import lombok.experimental.NonFinal;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import ru.novacore.events.EventUpdate;
import ru.novacore.events.TickEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;

@FunctionInfo(name = "Sprint", category = Category.Movement)
public class AutoSprint extends Function {
    @Setter
    @NonFinal
    boolean emergencyStop = false;

    public BooleanSetting saveSprint = new BooleanSetting("Keep Sprint", true);
    private BooleanSetting ignoreBadEffects = new BooleanSetting("Игнорировать плохие эффекты", false);
    public AutoSprint() {
        addSettings(saveSprint, ignoreBadEffects);
    }

    boolean wasSprinting = false;

    @Subscribe
    public void onUpdate(EventUpdate event) {
        boolean canSprint = canStartSprinting();

        if (canSprint && !mc.player.collidedHorizontally) {
            if (!mc.player.isSprinting()) {
                mc.player.setSprinting(true);
            }
        } else if (!canSprint || emergencyStop) {
            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
        }

        emergencyStop = false;
    }

    private boolean canStartSprinting() {
        return (mc.player.moveForward > 0 || mc.player.isSwimming()) &&
                (ignoreBadEffects.get() || !mc.player.isPotionActive(Effects.BLINDNESS)) && mc.player.getFoodStats().getFoodLevel() > 6;
    }

}
