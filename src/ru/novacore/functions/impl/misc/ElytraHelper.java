package ru.novacore.functions.impl.misc;

import ru.novacore.events.EventHandler;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import ru.novacore.events.input.EventKey;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BindSetting;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.player.InventoryUtil;

@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionInfo(name = "ElytraSwap", category = Category.Misc)
public class ElytraHelper extends Function {
    final BindSetting swapChestKey = new BindSetting("Кнопка свапа", -1);
    final BindSetting fireWorkKey = new BindSetting("Кнопка фейерверка", -1);
    final BooleanSetting autoFly = new BooleanSetting("Авто-взлёт", true);
    final BooleanSetting autoFirework = new BooleanSetting("Авто-фейерверк", false).setVisible(autoFly::get);


    public ElytraHelper() {
        addSettings(swapChestKey, fireWorkKey, autoFly,autoFirework);
    }

    ItemStack currentStack = ItemStack.EMPTY;
    public static StopWatch stopWatch = new StopWatch();
    public static StopWatch fireworkTimer = new StopWatch();
    boolean fireworkUsed;

    @EventHandler
    public void onEventKey(EventKey eventKey) {
        if (eventKey.getKey() == swapChestKey.get()) {
            changeChestPlate(currentStack);
        }

        if (eventKey.getKey() == fireWorkKey.get() && mc.player.isElytraFlying()) {
            fireworkUsed = true;
        }
    }

    boolean startFallFlying;

    @EventHandler
    public void onUpdt(EventUpdate eventUpdate) {
        this.currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (this.autoFly.get() && this.currentStack.getItem() == Items.ELYTRA) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else if (ElytraItem.isUsable(this.currentStack) && !mc.player.isElytraFlying()) {
                mc.player.startFallFlying();
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                if (fireworkTimer.hasElapsed(100)) {
                    InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET, -1);
                    fireworkTimer.reset();
                }
            }
        }

        if (fireworkUsed) {
            int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true);
            int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, false);

            if (invSlot == -1 && hbSlot == -1) {
                fireworkUsed = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.FIREWORK_ROCKET)) {
                InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET, InventoryUtil.getItemIndex(currentStack.getItem()));
            }
            fireworkUsed = false;
        }
    }

    private void changeChestPlate(ItemStack stack) {
        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                currentStack = mc.player.inventory.getStackInSlot(elytraSlot).copy();
                InventoryUtil.moveItem(elytraSlot, 6);
                return;
            }
        }
        int armorSlot = getChestPlateSlot();
        if (armorSlot >= 0) {
            currentStack = mc.player.inventory.getStackInSlot(armorSlot).copy();
            InventoryUtil.moveItem(armorSlot, 6);
        }
    }


    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    if (i < 9) {
                        i += 36;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        stopWatch.reset();
        super.onDisable();
    }

    private int getItemSlot(Item input) {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }
}
