package ru.novacore.functions.impl.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import ru.novacore.NovaCore;
import ru.novacore.command.friends.FriendStorage;
import ru.novacore.events.EventInput;
import ru.novacore.events.EventMotion;
import ru.novacore.events.EventPacket;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.impl.movement.AutoSprint;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.SensUtils;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.player.DamagePlayerUtil;
import ru.novacore.utils.player.InventoryUtil;
import ru.novacore.utils.player.MouseUtil;
import ru.novacore.utils.player.MoveUtils;
import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.hypot;
import static java.lang.Math.nextUp;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;
// by lapycha and artem
@FunctionInfo(name = "AttackAura", category = Category.Combat)
public class AttackAura extends Function {
    @Getter
    private final ModeSetting bypass = new ModeSetting("Обход Античита","ReallyWorld","ReallyWorld","LegendsGrief");
    private final ModeSetting type = new ModeSetting("Тип", "Плавная", "Плавная", "Резкая");
    private final SliderSetting attackRange = new SliderSetting("Дистанция аттаки", 3f, 3f, 6f, 0.1f);
    private final SliderSetting rotateRange = new SliderSetting("Дистанция наведение", 1.0f, 0.0f, 3.0f, 0.1f).setVisible(() -> type.is("Плавная"));
    private final SliderSetting elytraRotateRange = new SliderSetting("Элитра наведение", 30.0f, 0.0f, 70.0f, 1.0f);

    final ModeListSetting targets = new ModeListSetting("Таргеты",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Мобы", false),
            new BooleanSetting("Друзья", true),
            new BooleanSetting("Голые", true),
            new BooleanSetting("Невидимки", true));

    @Getter
    final ModeListSetting options = new ModeListSetting("Настройки",
            new BooleanSetting("Только криты", true),
            new BooleanSetting("Ломать щит", true),
            new BooleanSetting("Отжимать щит", true),
            new BooleanSetting("Синхронизировать атаку с ТПС", false),
            new BooleanSetting("Фокусировать одну цель", true),
            new BooleanSetting("Коррекция движения", true),
            new BooleanSetting("Бить через стены", true),
            new BooleanSetting("Не бить когда ешь", true),
            new BooleanSetting("Не бить с щитом", true));

    final ModeSetting correctionType = new ModeSetting("Тип коррекции", "Незаметный", "Незаметный", "Сфокусированный");

    final BooleanSetting forwardboolean = new BooleanSetting("Обгонять", true);
    final SliderSetting forward = new SliderSetting("Значение обгона", 0.0f, 0.0f, 3.0f, 0.1f).setVisible(() -> forwardboolean.get());
    final BooleanSetting smartCrits = new BooleanSetting("Умные криты", false).setVisible(() -> options.get(0).get());
    final BooleanSetting rayTraceCheck = new BooleanSetting("Проверка на наведение", true).setVisible(() -> type.is("Плавная"));
    final BooleanSetting chekarmor = new BooleanSetting("Приоритет на элитры", true);
    final ModeSetting sprintSetting = new ModeSetting("Сброс спринта", "Легитный", "Легитный", "Пакетный");

    @Getter
    private final StopWatch stopWatch = new StopWatch();
    @Getter
    private Vector2f rotateVector = new Vector2f(0, 0);
    @Getter
    private LivingEntity target;
    private Entity selected;

    long cpsLimit = 0;

    int ticks = 0;
    boolean isRotated;

    final AutoPotion autoPotion;

    public AttackAura(AutoPotion autoPotion) {
        this.autoPotion = autoPotion;
        addSettings(bypass,type, attackRange,rotateRange,elytraRotateRange, targets, options, correctionType,sprintSetting, smartCrits, forward,chekarmor, rayTraceCheck, forwardboolean);
    }

    @Subscribe
    public void onInput(EventInput eventInput) {
        if (options.getValueByName("Коррекция движения").get() && correctionType.is("Незаметный") && target != null && mc.player != null) {
            MoveUtils.fixMovement(eventInput, rotateVector.x);
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (options.getValueByName("Фокусировать одну цель").get() && (target == null || !isValid(target)) || !options.getValueByName("Фокусировать одну цель").get()) {
            updateTarget();
        }

        if (target == null || autoPotion.isActive()) {
            cpsLimit = System.currentTimeMillis();
            reset();
            return;
        }
        isRotated = false;

        boolean elytraTarget = mc.player.isElytraFlying();
        if (type.is("Резкая") && !elytraTarget) {
            if (shouldAttack()) {
                updateAttack();
                ticks = 2;
            }
            if (ticks > 0) {
                updateRotation(true,180.0f, 90.0f);
                --ticks;
            } else {
                reset();
            }
        }
        if (type.is("Плавная") || elytraTarget) {
            if (shouldAttack()) {
                updateAttack();
            }
            updateRotation(false, 180.0f, 90.0f);
        }
    }
    @Subscribe
    private void onWalking(EventMotion e) {
        if (target == null || autoPotion.isState() && autoPotion.isActive()) return;

        float yaw = rotateVector.x;
        float pitch = rotateVector.y;

        e.setYaw(yaw);
        e.setPitch(pitch);
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = MathUtil.calculateCorrectYawOffset(yaw);
        mc.player.rotationPitchHead = pitch;
    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof LivingEntity living && isValid(living)) {
                targets.add(living);
            }
        }

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        if (targets.size() == 1) {
            target = targets.get(0);
            return;
        }

        targets.sort(Comparator.comparingDouble(object -> {
            if (object instanceof PlayerEntity player) {
                return -getEntityArmor(player);
            }
            if (object instanceof LivingEntity base) {
                return -base.getTotalArmorValue();
            }
            return 0.0;
        }).thenComparing((object, object2) -> {
            double d2 = getEntityHealth((LivingEntity) object);
            double d3 = getEntityHealth((LivingEntity) object2);
            return Double.compare(d2, d3);
        }).thenComparing((object, object2) -> {
            double d2 = mc.player.getDistance((LivingEntity) object);
            double d3 = mc.player.getDistance((LivingEntity) object2);
            return Double.compare(d2, d3);
        }));

        target = targets.get(0);
    }

    private boolean shouldAttack() {
        return target != null && shouldPlayerFalling() && cpsLimit <= System.currentTimeMillis();
    }

    float lastYaw, lastPitch;

    //chat gpt!!!!!
    private boolean disableForward;
    private final StopWatch forwardTimer = new StopWatch();

    private void updateRotation(boolean attack, float rotationYawSpeed, float rotationPitchSpeed) {
        Vector3d vec3d = MathUtil.getVector(target);

        double yDiff = target.getPosY() - mc.player.getPosY();
        boolean notSwimmingFlying = !target.isSwimming() && !target.isElytraFlying();

        if (yDiff > 0.0 && yDiff < 1.1 && notSwimmingFlying) {
            vec3d = vec3d.subtract(0, yDiff, 0);
        } else if (yDiff >= 1.1 && notSwimmingFlying) {
            vec3d = vec3d.subtract(0, 1.1, 0);
        } else if (yDiff < 0.0 && -yDiff <= 0.25 && notSwimmingFlying) {
            vec3d = vec3d.add(0, target.isSneaking() ? 0 : -yDiff, 0);
        } else if (yDiff < -0.25 && notSwimmingFlying) {
            vec3d = vec3d.add(0, target.isSneaking() ? 0 : 0.25, 0);
        }

        isRotated = true;

        boolean bothFlying = mc.player.isElytraFlying() && target.isElytraFlying();

        if (forwardboolean.get() && !disableForward && bothFlying) {
            double boostMultiplier = forward.get().doubleValue();

            Vector3d targetMotion = target.getMotion();
            double predictionFactor = mc.player.isElytraFlying() && target.isElytraFlying() ? boostMultiplier : boostMultiplier * 0.5;

            Vector3d predictedMotion = targetMotion.scale(predictionFactor);
            vec3d = vec3d.add(predictedMotion);

            double distance = mc.player.getDistance(target);
            double angleCorrection = Math.min(distance / 20.0, 1.0);
            vec3d = vec3d.add(targetMotion.normalize().scale(angleCorrection * 0.1));
        }

        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec3d.z, vec3d.x)) - 90);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec3d.y, hypot(vec3d.x, vec3d.z))));

        float yawDelta = (wrapDegrees(yawToTarget - rotateVector.x));
        float pitchDelta = (wrapDegrees(pitchToTarget - rotateVector.y));

        float rotationDifference = (float)Math.hypot(Math.abs(pitchDelta), Math.abs(yawDelta));

        int roundedYaw = (int) yawDelta;

        switch (type.get()) {
            case "Плавная" -> {
                float limitedYaw = Math.abs(yawDelta / rotationDifference) * rotationYawSpeed;
                float limitedPitch = Math.abs(pitchDelta / rotationDifference) * rotationPitchSpeed;

                if (attack && selected != target) {
                    limitedPitch = Math.max(Math.abs(pitchDelta), 1.0f);
                } else {
                    limitedYaw /= 3f;
                }

                float finalYaw = rotateVector.x + Math.min(Math.max(yawDelta, -limitedYaw), limitedYaw);
                float finalPitch = MathHelper.clamp(rotateVector.y + Math.min(Math.max(pitchDelta, -limitedPitch), limitedPitch), -89, 89.0F);

                float gcd = SensUtils.getGCDValue();

                finalYaw -= (finalYaw - rotateVector.x) % gcd;
                finalPitch -= (finalPitch - rotateVector.y) % gcd;

                lastYaw = finalYaw;
                lastPitch = finalPitch;
                rotateVector = new Vector2f(finalYaw, finalPitch);
                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = finalYaw;
                }
            }
            case "Резкая" -> {
                float yaw = rotateVector.x + roundedYaw;
                float pitch = clamp(rotateVector.y + pitchDelta, -90, 90);

                float gcd = SensUtils.getGCDValue();
                yaw -= (yaw - rotateVector.x) % gcd;
                pitch -= (pitch - rotateVector.y) % gcd;

                rotateVector = new Vector2f(yaw, pitch);

                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }
        }
    }


    //РАДИАЦИЯ АПАСНА!!!!!
    @Subscribe
    public void onPacketEvent(EventPacket eventPacket) {
        if (mc.player.hurtTime > 0) {
            disableForward = true;
            forwardTimer.reset();
        }
        if (forwardTimer.isReached(1000)) {
            disableForward = false;
        }
    }

    private void updateAttack() {
        selected = MouseUtil.getMouseOver(target, rotateVector.x, rotateVector.y, attackRange.get());
        if ((selected == null || selected != target) && !mc.player.isElytraFlying() && rayTraceCheck.get()) {
            return;
        }

        if (options.getValueByName("Не бить когда ешь").get() && mc.player.isHandActive() && mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT) {
            return;
        }
        if (options.getValueByName("Не бить с щитом").get() && mc.player.isBlocking()) {
            return;
        }

        if (mc.player.isBlocking() && options.getValueByName("Отжимать щит").get()) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }

        boolean sprinting = sprintSetting.is("Пакетный") && mc.player.serverSprintState;

        if (sprinting) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
        }

        AutoSprint autoSprint = NovaCore.getInstance().getFunctionRegistry().getAutoSprint();

        if (sprintSetting.is("Легитный") && autoSprint.isState()) {
            autoSprint.setEmergencyStop(true);
        }
        if (mc.player.isSprinting()) mc.player.setSprinting(false);

        if (bypass.get().equals("ReallyWorld")) {
            cpsLimit = System.currentTimeMillis() + 500;
            if(mc.player.isSprinting())
                mc.player.setSprinting(false);

        } else if (bypass.get().equals("LegendsGrief")) {
            cpsLimit = System.currentTimeMillis() + 500+ (int) (Math.random() * 71);
            if ((double) mc.timer.timerSpeed == 1.0) {
                mc.timer.timerSpeed = 1.005F;
                if(mc.player.isSprinting()) mc.player.setSprinting(false);
            }
        }

        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);

        if (mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }

        if (target instanceof PlayerEntity player && options.getValueByName("Ломать щит").get()) {
            breakShieldPlayer(player);
        }

        if (sprinting) {
            mc.player.setSprinting(false);
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
        }
    }


    private boolean shouldPlayerFalling() {
        boolean onSpace = smartCrits.get() && mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown();

        boolean reasonForAttack = mc.player.isPotionActive(Effects.LEVITATION)
                || mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isPotionActive(Effects.SLOW_FALLING)
                || mc.player.isOnLadder()
                || mc.player.isInWater() && mc.player.areEyesInFluid(FluidTags.WATER)
                || mc.player.isInLava() && mc.player.areEyesInFluid(FluidTags.LAVA)
                || mc.player.isPassenger()
                || mc.player.abilities.isFlying
                || mc.player.isElytraFlying()
                || mc.player.isSwimming();

        if (mc.player.getDistance(target) > attackRange.get() || mc.player.getCooledAttackStrength(options.getValueByName("Синхронизировать атаку с ТПС").get() ? NovaCore.getInstance().getTpsCalc().getAdjustTicks() : 1.5f) < 0.93f) return false;

        if (!reasonForAttack && options.get(0).get()) return onSpace || !mc.player.isOnGround() && mc.player.fallDistance > 0;

        return true;
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;
        if (entity instanceof ArmorStandEntity) return false;
        if (!options.getValueByName("Бить через стены").get() && !mc.player.canEntityBeSeen(entity)) return false;

        if (entity.ticksExisted < 3) return false;

        if (mc.player.getDistanceEyePos(entity) >
                attackRange.get() + (!type.is("Резкая") ? rotateRange.get() : 0)
                        + (mc.player.isElytraFlying() ? elytraRotateRange.get() : 0.0f)) return false;

        if (entity instanceof PlayerEntity p) {
            if (AntiBot.isBot(entity)) {
                return false;
            }
            if (!targets.getValueByName("Друзья").get() && FriendStorage.isFriend(p.getName().getString())) {
                return false;
            }
            if (p.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) return false;
            if (mc.player.isElytraFlying() && chekarmor.get() && !p.isElytraFlying()) return false;
        }

        if (entity instanceof PlayerEntity && !targets.getValueByName("Игроки").get()) {
            return false;
        }

        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !targets.getValueByName("Голые").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && !targets.getValueByName("Невидимки").get()) {
            return false;
        }

        if (entity instanceof MonsterEntity || entity instanceof AnimalEntity && !targets.getValueByName("Мобы").get()) {
            return false;
        }

        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private void breakShieldPlayer(PlayerEntity entity) {
        if (entity.isBlocking()) {
            int invSlot = InventoryUtil.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryUtil.getInstance().getAxeInInventory(true);

            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryUtil.getInstance().findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);

                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));

                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
            }

            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }
    }


    private void reset() {
        if (options.getValueByName("Коррекция движения").get()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
        }
        rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
        target = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        cpsLimit = System.currentTimeMillis();
        target = null;
    }

    private double getEntityArmor(PlayerEntity entityPlayer2) {
        double d2 = 0.0;
        for (int i2 = 0; i2 < 4; ++i2) {
            ItemStack is = entityPlayer2.inventory.armorInventory.get(i2);
            if (!(is.getItem() instanceof ArmorItem)) continue;
            d2 += getProtectionLvl(is);
        }
        return d2;
    }
    //dsad
    private double getProtectionLvl(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem i) {
            double damageReduceAmount = i.getDamageReduceAmount();
            if (stack.isEnchanted()) {
                damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25;
            }
            return damageReduceAmount;
        }
        return 0;
    }

    private double getEntityHealth(LivingEntity ent) {
        if (ent instanceof PlayerEntity player) {
            return (double) (player.getHealth() + player.getAbsorptionAmount()) * (getEntityArmor(player) / 20.0);
        }
        return ent.getHealth() + ent.getAbsorptionAmount();
    }
}
