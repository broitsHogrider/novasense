package ru.novacore.functions.impl.combat;

import com.google.common.eventbus.Subscribe;
import ru.novacore.events.*;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BindSetting;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.player.InventoryUtil;
import ru.novacore.utils.player.MoveUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.*;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionInfo(name = "AutoSwap", category = Category.Combat)
public class AutoSwap extends Function {
    private final List<IPacket<?>> packet = new ArrayList<>();
    final ModeSetting swapMode = new ModeSetting("Тип", "Умный", "Умный", "По бинду");
    final ModeSetting itemType = new ModeSetting("Предмет", "Щит", "Щит", "Геплы", "Тотем", "Шар","Руна","Арбалет","Фейверк");
    final ModeSetting swapType = new ModeSetting("Свапать на", "Геплы", "Щит", "Геплы", "Тотем", "Шар","Руна","Арбалет","Фейверк");
    private final BooleanSetting mode = new BooleanSetting("Grim", true);

    final BindSetting keyToSwap = new BindSetting("Кнопка", -1).setVisible(() -> swapMode.is("По бинду"));
    final SliderSetting health = new SliderSetting("Здоровье", 11.0F, 5.0F, 19.0F, 0.5F).setVisible(() -> swapMode.is("Умный"));
    final StopWatch stopWatch = new StopWatch();
    boolean shieldIsCooldown;
    int oldItem = -1;
    final StopWatch delay = new StopWatch();
    final AutoTotem autoTotem;


    public AutoSwap(AutoTotem autoTotem) {
        this.autoTotem = autoTotem;
        addSettings(swapMode, itemType, swapType, keyToSwap, health);
    }

    @Subscribe
    public void onEventKey(EventKey e) {
        if (!swapMode.is("По бинду")) {
            return;
        }
        ItemStack offhandItemStack = mc.player.getHeldItemOffhand();
        boolean isOffhandNotEmpty = !(offhandItemStack.getItem() instanceof AirItem);
        if (e.isKeyDown(keyToSwap.get()) && stopWatch.isReached(150)) {
            Item currentItem = offhandItemStack.getItem();
            boolean isHoldingSwapItem = currentItem == getSwapItem();
            boolean isHoldingSelectedItem = currentItem == getSelectedItem();
            int selectedItemSlot = getSlot(getSelectedItem());
            int swapItemSlot = getSlot(getSwapItem());
            if (mode.get()){
            }
            if (selectedItemSlot >= 0) {
                if (!isHoldingSelectedItem) {
                    InventoryUtil.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                    stopWatch.reset();
                    return;
                }
            }
            if (mode.get()){
            }
            if (swapItemSlot >= 0) {
                if (!isHoldingSwapItem) {
                    InventoryUtil.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                    stopWatch.reset();
                }
            }
        }
    }
    @Subscribe
    private void onPacket(CEntityActionPacket actionPacket) {
        switch (actionPacket.getAction()) {
            case PRESS_SHIFT_KEY:
                mc.player.setSneaking(true);
                break;
        }
    }


    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
    public StopWatch wait = new StopWatch();

    @Subscribe
    private void onCooldown(EventCooldown e) {
        shieldIsCooldown = isCooldown(e);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!swapMode.is("Умный")) {
            return;
        }

        Item currentItem = mc.player.getHeldItemOffhand().getItem();
        if (stopWatch.isReached(350L)) {
            swapIfShieldIsBroken(currentItem);
            swapIfHealthToLow(currentItem);
            stopWatch.reset();
        }
        boolean isRightClickWithGoldenAppleActive = false;

        if (currentItem == Items.GOLDEN_APPLE && !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE)) {
            isRightClickWithGoldenAppleActive = mc.gameSettings.keyBindUseItem.isKeyDown();
        }


        if (isRightClickWithGoldenAppleActive) {
            stopWatch.reset();
        }
        if (mode.get()){
            if(mc.player.isSprinting());
            mc.player.setSprinting(false);
            mc.player.motionX = 0;
            mc.player.motionY = 0;
            mc.player.motionZ = 0;
        }
    }

    @Override
    public void onDisable() {
        shieldIsCooldown = false;
        oldItem = -1;
        super.onDisable();
    }

    private void swapIfHealthToLow(Item currentItem) {
        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        boolean isHoldingGoldenApple = currentItem == getSwapItem();
        boolean isHoldingSelectedItem = currentItem == getSelectedItem();
        boolean gappleIsNotCooldown = !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);

        int swapItemSlot = getSlot(getSwapItem());
        int selectedItemSlot = getSlot(getSelectedItem());


        if (isLowHealth() && !isHoldingGoldenApple && isHoldingSelectedItem) {
            if (itemType.is("Шар") && selectedItemSlot >= 0) {
                InventoryUtil.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                if (isOffhandNotEmpty && oldItem == -1) {
                    oldItem = selectedItemSlot;
                }
            } else {
                InventoryUtil.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                if (isOffhandNotEmpty && oldItem == -1) {
                    oldItem = swapItemSlot;
                }
            }
        } else if (!isLowHealth() && isHoldingGoldenApple && oldItem >= 0) {
            InventoryUtil.moveItem(oldItem, 45, isOffhandNotEmpty);
            oldItem = -1;
        }
    }

    private void swapIfShieldIsBroken(Item currentItem) {
        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        boolean isHoldingGoldenApple = currentItem == getSwapItem();
        boolean isHoldingSelectedItem = currentItem == getSelectedItem();
        boolean gappleIsNotCooldown = !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);

        int swapItemSlot = getSlot(getSwapItem());
        int selectedItemSlot = getSlot(getSelectedItem());


        if (shieldIsCooldown && !isHoldingGoldenApple && isHoldingSelectedItem && gappleIsNotCooldown) {
            if (itemType.is("Шар") && selectedItemSlot >= 0) {
                InventoryUtil.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                if (isOffhandNotEmpty && oldItem == -1) {
                    oldItem = selectedItemSlot;
                }
            } else {
                InventoryUtil.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                if (isOffhandNotEmpty && oldItem == -1) {
                    oldItem = swapItemSlot;
                }
            }
        } else if (!shieldIsCooldown && isHoldingGoldenApple && oldItem >= 0) {
            InventoryUtil.moveItem(oldItem, 45, isOffhandNotEmpty);
            oldItem = -1;
        }
    }

    private boolean isLowHealth() {
        float currentHealth = mc.player.getHealth() + (mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f);
        return currentHealth <= health.get();
    }

    private boolean isCooldown(EventCooldown cooldown) {
        Item item = cooldown.getItem();


        if (!itemType.is("Shield")) {
            return false;
        } else {
            return cooldown.isAdded() && item instanceof ShieldItem;
        }
    }

    private Item getSwapItem() {
        return getItemByType(swapType.get());
    }

    private Item getSelectedItem() {
        return getItemByType(itemType.get());
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Щит" -> Items.SHIELD;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Шар" -> Items.PLAYER_HEAD;
            case "Руна" -> Items.POPPED_CHORUS_FRUIT;
            case "Арбалет" -> Items.CROSSBOW;
            case "Фейверк" -> Items.FIREWORK_ROCKET;
            default -> Items.AIR;
        };
    }

    private int getSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                if (mc.player.inventory.getStackInSlot(i).isEnchanted()) {
                    finalSlot = i;
                    break;
                } else {
                    finalSlot = i;
                }
            }
        }
        if (finalSlot < 9 && finalSlot != -1) {
            finalSlot = finalSlot + 36;
        }
        return finalSlot;
    }
    @Subscribe
    public void onPacket(EventPacket e) {
        if (mode.get()) {
            if (e.getPacket() instanceof CClickWindowPacket p && MoveUtils.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }
            }
        }
    }
}