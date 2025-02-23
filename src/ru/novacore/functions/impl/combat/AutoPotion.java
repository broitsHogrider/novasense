package ru.novacore.functions.impl.combat;

import ru.novacore.events.EventHandler;
import ru.novacore.events.input.EventMotion;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.client.PotionUtil;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.player.InventoryUtil;
import ru.novacore.utils.player.MoveUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@FunctionInfo(name = "AutoPotion", category = Category.Combat)
public class AutoPotion extends Function {

    private static final ModeListSetting buff = new ModeListSetting("Бросать",
            new BooleanSetting("Сила", true),
            new BooleanSetting("Скорость", true),
            new BooleanSetting("Огнестойкость", true),
            new BooleanSetting("Исцеление", true));
    private final SliderSetting health = new SliderSetting("Здоровье", 9, 1, 20, 0.5f).setVisible(() -> buff.getValueByName("Исцеление").get());

    public AutoPotion() {
        addSettings(buff, health);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (this.isActive()) {
            for (PotionType potionType : PotionType.values()) {
                isActivePotion = potionType.isEnabled();
            }
        } else {
            isActivePotion = false;
        }

        if (this.isActive() && mc.player.isOnGround() && previousPitch == mc.player.lastReportedPitch) {
            int oldItem = mc.player.inventory.currentItem;
            this.selectedSlot = -1;

            for (PotionType potionType : PotionType.values()) {
                if (potionType.isEnabled()) {
                    int slot = this.findPotionSlot(potionType);
                    if (this.selectedSlot == -1) {
                        this.selectedSlot = slot;
                    }

                    this.isActive = true;
                }
            }

            if (this.selectedSlot > 8) {
                mc.playerController.pickItem(this.selectedSlot);
            }

            mc.player.connection.sendPacket(new CHeldItemChangePacket(oldItem));
        }

        if (time.isReached(500L)) {
            try {
                this.reset();
                this.selectedSlot = -2;
            } catch (Exception ignored) {
            }
        }

        this.potionUtil.changeItemSlot(this.selectedSlot == -2);
    }

    @EventHandler
    public void onMotion(EventMotion e) {
        if (!this.isActive() || !mc.player.isOnGround()) {
            return;
        }

        float[] angles = new float[]{mc.player.rotationYaw, 90.0F};
        this.previousPitch = 90.0F;
        e.setYaw(angles[0]);
        e.setPitch(this.previousPitch);
        mc.player.rotationPitchHead = this.previousPitch;
        mc.player.rotationYawHead = angles[0];
        mc.player.renderYawOffset = angles[0];
    }

    public boolean isActive;
    private int selectedSlot;
    private float previousPitch;
    private StopWatch time = new StopWatch();
    private PotionUtil potionUtil = new PotionUtil();
    public boolean isActivePotion;

    private void reset() {
        for (PotionType potionType : PotionType.values()) {
            if (potionType.isPotionSettingEnabled().get()) {
                potionType.setEnabled(this.isPotionActive(potionType));
            }
        }
    }

    private int findPotionSlot(PotionType type) {
        int hbSlot = this.getPotionIndexHb(type.getPotionId());
        if (hbSlot != -1) {
            this.potionUtil.setPreviousSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            PotionUtil.useItem(Hand.MAIN_HAND);
            type.setEnabled(false);
            time.reset();
            return hbSlot;
        } else {
            int invSlot = this.getPotionIndexInv(type.getPotionId());
            if (invSlot != -1) {
                this.potionUtil.setPreviousSlot(mc.player.inventory.currentItem);
                mc.playerController.pickItem(invSlot);
                PotionUtil.useItem(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                type.setEnabled(false);
                time.reset();
                return invSlot;
            } else {
                return -1;
            }
        }
    }

    public boolean isActive() {
        for (PotionType potionType : PotionType.values()) {
            if (potionType.isPotionSettingEnabled().get() && potionType.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPotionActive(PotionType type) {
        if (mc.player.isPotionActive(type.getPotion())) {
            this.isActive = false;
            return false;
        } else {
            return this.getPotionIndexInv(type.getPotionId()) != -1 || this.getPotionIndexHb(type.getPotionId()) != -1;
        }
    }

    private int getPotionIndexHb(int id) {
        for (int i = 0; i < 9; ++i) {
            for (EffectInstance potion : PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() == Effect.get(id) && mc.player.inventory.getStackInSlot(i).getItem() == Items.SPLASH_POTION) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int getPotionIndexInv(int id) {
        for (int i = 9; i < 36; ++i) {
            for (EffectInstance potion : PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() == Effect.get(id) && mc.player.inventory.getStackInSlot(i).getItem() == Items.SPLASH_POTION) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public void onDisable() {
        isActive = false;
        super.onDisable();
    }

    enum PotionType {
        STRENGHT(Effects.STRENGTH, 5, () -> buff.getValueByName("Сила").get()),
        SPEED(Effects.SPEED, 1, () -> buff.getValueByName("Скорость").get()),
        FIRE_RESIST(Effects.FIRE_RESISTANCE, 12, () -> buff.getValueByName("Огнестойкость").get());

        private final Effect potion;
        private final int potionId;
        private final Supplier<Boolean> potionSetting;
        private boolean enabled;

        PotionType(Effect potion, int potionId, Supplier<Boolean> potionSetting) {
            this.potion = potion;
            this.potionId = potionId;
            this.potionSetting = potionSetting;
        }

        public Effect getPotion() {
            return this.potion;
        }

        public int getPotionId() {
            return this.potionId;
        }

        public Supplier<Boolean> isPotionSettingEnabled() {
            return this.potionSetting;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean var1) {
            this.enabled = var1;
        }

    }
}
