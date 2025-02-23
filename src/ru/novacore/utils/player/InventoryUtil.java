package ru.novacore.utils.player;

import net.minecraft.client.Minecraft;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import ru.novacore.events.server.EventPacket;
import ru.novacore.utils.client.IMinecraft;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.server.SHeldItemChangePacket;

public class InventoryUtil implements IMinecraft {

    @Getter
    private static InventoryUtil instance = new InventoryUtil();


    public static int findEmptySlot(boolean inHotBar) {
        int start = inHotBar ? 0 : 9;
        int end = inHotBar ? 9 : 45;

        for (int i = start; i < end; ++i) {
            if (!mc.player.inventory.getStackInSlot(i).isEmpty()) {
                continue;
            }

            return i;
        }
        return -1;
    }
        public static int findItem(Item item) {
            for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                    return i;
                }
            }
            return -1;
        }


    public static void moveItem(int from, int to, boolean air) {

        if (from == to)
            return;
        pickupItem(from, 0);
        pickupItem(to, 0);
        if (air)
            pickupItem(from, 0);
    }

    public static int getItemSlot(Item item) {
        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getItem() == item) return -2;
        }

        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == item) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) slot = slot + 36;

        return slot;
    }

    public static void moveItemTime(int from, int to, boolean air, int time) {

        if (from == to)
            return;
        pickupItem(from, 0, time);
        pickupItem(to, 0, time);
        if (air)
            pickupItem(from, 0, time);
    }

    public static void moveItem(int from, int to) {
        if (from == to)
            return;
        pickupItem(from, 0);
        pickupItem(to, 0);
        pickupItem(from, 0);
    }

    public static void pickupItem(int slot, int button) {
        mc.playerController.windowClick(0, slot, button, ClickType.PICKUP, mc.player);
    }

    public static void pickupItem(int slot, int button, int time) {
        mc.playerController.windowClickFixed(0, slot, button, ClickType.PICKUP, mc.player, time);
    }

    public static int getItemIndex(Item item) {
        for(int i = 0; i < 45; ++i) {
            if (Minecraft.getInstance().player.inventory.getStackInSlot(i).getItem() == item) {
                return i;
            }
        }

        return -1;
    }

    public static boolean doesHotbarHaveItem(Item item) {
        for(int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }

    public static void inventorySwapClick(Item item, int oldSlot) {
        boolean handActive = mc.player.isHandActive();
        if (getItemIndex(item) != -1) {
            if (mc.player.getHeldItem(net.minecraft.util.Hand.OFF_HAND).getItem() == item) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.OFF_HAND));
            } else {
                int slot;
                if (doesHotbarHaveItem(item)) {
                    for (slot = 0; slot < 9; ++slot) {
                        if (mc.player.inventory.getStackInSlot(slot).getItem() == item) {
                            if (slot != mc.player.inventory.currentItem) {
                                mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
                            }
                            // ���� ���� �������, ����������� �� OFF_HAND
                            if (handActive) {
                                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.OFF_HAND));
                            } else {
                                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                            }
                            if (slot != mc.player.inventory.currentItem) {
                                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                            }
                            break;
                        }
                    }
                } else {
                    for (slot = 0; slot < 36; ++slot) {
                        if (mc.player.inventory.getStackInSlot(slot).getItem() == item) {
                            int swapSlot = (mc.player.inventory.currentItem + 1) % 9;
                            int parsedOldSlot = oldSlot;
                            if (parsedOldSlot >= 36 && parsedOldSlot <= 44) {
                                parsedOldSlot -= 36;
                            } else {
                                parsedOldSlot = -1;
                            }

                            while (swapSlot == mc.player.inventory.currentItem || swapSlot == parsedOldSlot) {
                                swapSlot = (swapSlot + 1) % 9;
                            }

                            mc.playerController.windowClick(0, slot, swapSlot, ClickType.SWAP, mc.player);
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(swapSlot));
                            // ���� ���� �������, ����������� �� OFF_HAND
                            if (handActive) {
                                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.OFF_HAND));
                            } else {
                                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                            }
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                            mc.playerController.windowClick(0, slot, swapSlot, ClickType.SWAP, mc.player);
                            break;
                        }
                    }
                }
            }
        }
    }


    public int getAxeInInventory(boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;

        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    public int findBestSlotInHotBar() {
        int emptySlot = findEmptySlot();
        if (emptySlot != -1) {
            return emptySlot;
        } else {
            return findNonSwordSlot();
        }
    }

    private int findEmptySlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty() && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    private int findNonSwordSlot() {
        for (int i = 0; i < 9; i++) {
            if (!(mc.player.inventory.getStackInSlot(i).getItem() instanceof SwordItem) && !(mc.player.inventory.getStackInSlot(i).getItem() instanceof ElytraItem) && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    public int getSlotInInventory(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static int getSlotInInventoryOrHotbar(Item item, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static int getSlotInInventoryOrHotbar() {
        int firstSlot = 0;
        int lastSlot = 9;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {

            if (Block.getBlockFromItem(mc.player.inventory.getStackInSlot(i).getItem()) instanceof SlabBlock) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static class Hand {
        public static boolean isEnabled;
        private boolean isChangingItem;
        private int originalSlot = -1;

        public void onEventPacket(EventPacket eventPacket) {
            if (!eventPacket.isReceive()) {
                return;
            }
            if (eventPacket.getPacket() instanceof SHeldItemChangePacket) {
                this.isChangingItem = true;
            }
        }

        public void handleItemChange(boolean resetItem) {
            if (this.isChangingItem && this.originalSlot != -1) {
                isEnabled = true;
                mc.player.inventory.currentItem = this.originalSlot;
                if (resetItem) {
                    this.isChangingItem = false;
                    this.originalSlot = -1;
                    isEnabled = false;
                }
            }
        }

        public void setOriginalSlot(int slot) {
            this.originalSlot = slot;
        }
    }

}
