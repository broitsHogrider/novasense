package ru.novacore.functions.impl.movement;

import ru.novacore.events.EventHandler;
import lombok.Setter;
import lombok.experimental.NonFinal;
import net.minecraft.item.UseAction;
import net.minecraft.potion.Effects;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;

@FunctionInfo(name = "AutoSprint", category = Category.Movement)
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

    @EventHandler
    public void onUpdate(EventUpdate event) {
        boolean canSprint = canStartSprinting();

        if (canSprint && !mc.player.collidedHorizontally &&
                !mc.gameSettings.keyBindSneak.isKeyDown() &&
                !mc.gameSettings.keyBindInventory.isKeyDown() &&
                mc.player.getActiveItemStack().getUseAction() != UseAction.EAT) {

            if (!mc.player.isSprinting()) {
                mc.player.setSprinting(true);
            }
        } else {
            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
        }

        emergencyStop = false;
    }

    @Override
    public void onDisable() {
        mc.player.setSprinting(false);
        super.onDisable();
    }

    private boolean canStartSprinting() {
        return (mc.player.moveForward > 0 || mc.player.isSwimming()) &&
                (ignoreBadEffects.get() || !mc.player.isPotionActive(Effects.BLINDNESS)) && mc.player.getFoodStats().getFoodLevel() > 6;
    }
}
