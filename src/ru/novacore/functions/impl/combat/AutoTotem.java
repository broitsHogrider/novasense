package ru.novacore.functions.impl.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.potion.Effects;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.player.InventoryUtil;
import ru.novacore.utils.player.WorldUtils;

@FunctionInfo(name = "AutoTotem", category = Category.Combat)
public class AutoTotem extends Function {
    private final SliderSetting health = new SliderSetting("Здоровье", 3.5F, 1.0F, 20.0F, 0.05F);
    private final BooleanSetting swapBack = new BooleanSetting("Возвращать предмет", true);
    private final BooleanSetting noBallSwitch = new BooleanSetting("Не свапать если шар", false);
    
    private final ModeListSetting mode = new ModeListSetting("Настройки",
            new BooleanSetting("Золотые сердца", true),
            new BooleanSetting("Кристалл", true), 
            new BooleanSetting("Обсидиан", false),
            new BooleanSetting("Якорь", false),
            new BooleanSetting("Падение", true),
            new BooleanSetting("Кристалл в руке", true),
            new BooleanSetting("Здоровье на элитре", true));
    
    private final SliderSetting radiusExplosion = (new SliderSetting("Радиус до обсидиана", 6.0F, 1.0F, 8.0F, 1.0F)).setVisible(() -> this.mode.get(1).get());
    private final SliderSetting radiusObs = (new SliderSetting("Радиус до кристалла", 6.0F, 1.0F, 8.0F, 1.0F)).setVisible(() -> this.mode.get(2).get());
    private final SliderSetting radiusAnch = (new SliderSetting("Радиус до якоря", 6.0F, 1.0F, 8.0F, 1.0F)).setVisible(() -> this.mode.get(2).get());
    private final SliderSetting HPElytra = (new SliderSetting("Здоровье на элитре", 6.0F, 1.0F, 20.0F, 0.005F)).setVisible(() -> this.mode.getValueByName("Здоровье на элитре").get());
    private final SliderSetting DistanceFall = (new SliderSetting("Дистанция падение", 20.0F, 3.0F, 50.0F, 0.005F)).setVisible(() -> this.mode.getValueByName("Падение").get());
    int oldItem = -1;

    public AutoTotem() {
        this.addSettings(this.mode, this.health, this.swapBack, this.noBallSwitch, this.radiusExplosion, this.HPElytra, this.DistanceFall);
    }


    @Subscribe
    private void handleEventUpdate(EventUpdate event) {
        int slot = InventoryUtil.getItemSlot(Items.TOTEM_OF_UNDYING);
        boolean handNotNull = !(mc.player.getHeldItemOffhand().getItem() instanceof AirItem);
        boolean totemInHand = mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING || mc.player.getHeldItemMainhand().getItem() == Items.TOTEM_OF_UNDYING;
        if (this.condition()) {
            if (slot >= 0 && !totemInHand) {
                mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
                if (handNotNull && this.oldItem == -1) {
                    this.oldItem = slot;
                }
            }
        } else if (this.oldItem != -1 && this.swapBack.get()) {
            mc.playerController.windowClick(0, this.oldItem, 40, ClickType.SWAP, mc.player);
            this.oldItem = -1;
        }

    }

    private boolean condition() {
        float absorption = this.mode.get(0).get() && mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0F;
        if (mc.player.getHealth() + absorption <= this.health.getValue().floatValue()) {
            return true;
        } else {
            if (!this.isBall()) {
                if (this.checkCrystal()) {
                    return true;
                }

                if (this.checkObsidian()) {
                    return true;
                }

                if (this.checkAnchor()) {
                    return true;
                }

                if (this.checkPlayerItemCrystal()) {
                    return true;
                }
            }

            return this.checkHPElytra() ? true : this.checkFall();
        }
    }

    private boolean checkHPElytra() {
        if (!this.mode.getValueByName("Здоровье на элитре").get()) {
            return false;
        } else {
            return (mc.player.inventory.armorInventory.get(2)).getItem() == Items.ELYTRA && mc.player.getHealth() <= this.HPElytra.getValue().floatValue();
        }
    }

    private boolean checkPlayerItemCrystal() {
        if (!this.mode.getValueByName("Кристалл в руке").get()) {
            return false;
        } else {
            for(PlayerEntity entity : mc.world.getPlayers()) {
                if (mc.player != entity && (entity.getHeldItemOffhand().getItem() == Items.END_CRYSTAL || entity.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) && mc.player.getDistance(entity) < 6.0F) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean checkFall() {
        if (!this.mode.get(4).get()) {
            return false;
        } else {
            return mc.player.fallDistance > this.DistanceFall.getValue().floatValue();
        }
    }

    private boolean isBall() {
        if (this.mode.get(3).get() && mc.player.fallDistance > 5.0F) {
            return false;
        } else {
            return noBallSwitch.get() && mc.player.getHeldItemOffhand().getItem() instanceof SkullItem;
        }
    }

    private boolean checkObsidian() {
        if (!this.mode.get(2).get()) {
            return false;
        } else {
            return WorldUtils.TotemUtil.getBlock(this.radiusObs.getValue().floatValue(), Blocks.OBSIDIAN) != null;
        }
    }

    private boolean checkAnchor() {
        if (!this.mode.get(3).get()) {
            return false;
        } else {
            return WorldUtils.TotemUtil.getBlock(this.radiusAnch.getValue().floatValue(), Blocks.RESPAWN_ANCHOR) != null;
        }
    }

    private boolean checkCrystal() {
        if (!this.mode.get(1).get()) {
            return false;
        } else {
            for(Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof EnderCrystalEntity && mc.player.getDistance(entity) <= this.radiusExplosion.getValue().floatValue()) {
                    return true;
                }

                if ((entity instanceof TNTEntity || entity instanceof TNTMinecartEntity) && mc.player.getDistance(entity) <= this.radiusExplosion.getValue().floatValue()) {
                    return true;
                }
            }

            return false;
        }
    }

    private void reset() {
        this.oldItem = -1;
    }

    @Override
    public void onEnable() {
        this.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }
}