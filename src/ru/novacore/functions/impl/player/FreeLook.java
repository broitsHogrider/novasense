package ru.novacore.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import ru.novacore.NovaCore;
import ru.novacore.events.EventMotion;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import net.minecraft.client.settings.PointOfView;

/**
 * @author Ieo117
 * @created 24.07.2024, on 11:10:07
 */
@FunctionInfo(name = "FreeLook", category = Category.Misc)
public class FreeLook extends Function {
    public SliderSetting pDistanceTo = new SliderSetting("Дистанция до камеры", 7.5f, 1.0f, 25.0f, 0.5f);

    public BooleanSetting free = new BooleanSetting("Свободная камера", false);

    public FreeLook() {
        addSettings(pDistanceTo, free);
    }

    private float startYaw, startPitch;

    @Override
    public void onEnable() {
        if (isFree()) {
            startYaw = mc.player.rotationYaw;
            startPitch = mc.player.rotationPitch;
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (isFree()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
            mc.gameSettings.setPointOfView(PointOfView.FIRST_PERSON);
            mc.player.rotationYaw = startYaw;
            mc.player.rotationPitch = startPitch;
        }
        super.onDisable();
    }


    @Subscribe
    public void onUpdate(EventUpdate e) {
        AttackAura aura = NovaCore.getInstance().getFunctionRegistry().getAttackAura();
        if (free.get()) {
            if (!aura.isState() && aura.getTarget() == null) {
                mc.gameSettings.setPointOfView(PointOfView.THIRD_PERSON_BACK);
                mc.player.rotationYawOffset = startYaw;
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (free.get()) {
            e.setYaw(startYaw);
            e.setPitch(startPitch);
            e.setOnGround(mc.player.isOnGround());
            mc.player.rotationYawHead = mc.player.rotationYawOffset;
            mc.player.renderYawOffset = mc.player.rotationYawOffset;
            mc.player.rotationPitchHead = startPitch;
        }
    }

    public boolean isFree() {
        return free.get();
    }
}