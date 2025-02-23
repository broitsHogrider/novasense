package ru.novacore.functions.impl.combat;

import ru.novacore.events.EventHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import ru.novacore.events.render.EventDisplay;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.player.InventoryUtil;
import ru.novacore.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@FunctionInfo(name = "AutoTotem", category = Category.Combat)
public class AutoTotem extends Function {
    private final SliderSetting health = new SliderSetting("Здоровье",  4F, 1F, 20F, 1F);
    private final BooleanSetting drawCounter = new BooleanSetting("Отображать кол-во",  true);
    private final BooleanSetting swapBack = new BooleanSetting("Возвращать",  true);

    private final BooleanSetting noBallSwitch = new BooleanSetting("Не брать при шаре", false);

    private final ModeListSetting mode = new ModeListSetting("Условие",
                    new BooleanSetting("Поглощение", true),
                    new BooleanSetting("Обсидиан", false),
                    new BooleanSetting("Кристалл", true),
                    new BooleanSetting("Якорь", true),
                    new BooleanSetting("Падение", true)
            );

    private final SliderSetting obsidianRadius = new SliderSetting("Радиус от обсы", 6, 1, 8, 1).setVisible(() -> !mode.getValueByName("Обсидиан").get());
    private final SliderSetting crystalRadius = new SliderSetting("Радиус от кристалла", 6, 1, 8, 1).setVisible(() -> !mode.getValueByName("Кристалл").get());
    private final SliderSetting anchorRadius = new SliderSetting("Радиус от якоря", 6, 1, 8, 1).setVisible(() -> !mode.getValueByName("Якорь").get());

    private int swapBackSlot = -1;
    private final ItemStack stack = new ItemStack(Items.TOTEM_OF_UNDYING);

    public AutoTotem() {
        addSettings(health, drawCounter, swapBack, noBallSwitch, mode, obsidianRadius, crystalRadius, anchorRadius);
    }

    @EventHandler
    private void onUpdate(EventUpdate eventUpdate) {
        int slot = InventoryUtil.getItemSlot(Items.TOTEM_OF_UNDYING);
        boolean totemInHand = mc.player.getHeldItemOffhand().getItem().equals(Items.TOTEM_OF_UNDYING);
        boolean handNotNull = !(mc.player.getHeldItemOffhand().getItem() instanceof AirItem);

        if (condition()) {
            if (slot >= 0) {
                if (!totemInHand) {
                    mc.playerController.windowClick(0, slot, 1, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 1, ClickType.PICKUP, mc.player);
                    if (handNotNull && swapBack.getValue()) {
                        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                        if (swapBackSlot == -1) swapBackSlot = slot;
                    }
                }
            }

        } else if (swapBackSlot >= 0) {
            mc.playerController.windowClick(0, swapBackSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
            if (handNotNull && swapBack.getValue()) {
                mc.playerController.windowClick(0, swapBackSlot, 0, ClickType.PICKUP, mc.player);
            }
            swapBackSlot = -1;
        }
    }
    
    @EventHandler
    private void onDisplay(EventDisplay eventDisplay) {
        if (!drawCounter.getValue())
            return;

        if (getTotemCount() > 0) {
            Fonts.sfbold.drawTextWithOutline(eventDisplay.getMatrixStack(), getTotemCount() + "x", mc.getMainWindow().getScaledWidth() / 2f + 12F,
                    mc.getMainWindow().getScaledHeight() / 2f + 24, -1, 6f, 0.05f);
            GlStateManager.pushMatrix();
            GlStateManager.disableBlend();
            mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, (int) (mc.getMainWindow().getScaledWidth() / 2F - 8), (int) (mc.getMainWindow().getScaledHeight() / 2F + 20));
            GlStateManager.popMatrix();
        }
    }
    
    private boolean condition() {
        float health = mc.player.getHealth();
        if (mode.getValueByName("Поглощение").get()) {
            health += mc.player.getAbsorptionAmount();
        }

        if (this.health.get() >= health) {
            return true;
        }

        if (!isBall()) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (mode.getValueByName("Кристалл").get()) {
                    if (entity instanceof EnderCrystalEntity && mc.player.getDistanceSq(entity) <= crystalRadius.get()) {
                        return true;
                    }
                }
            }

            if (mode.getValueByName("Якорь").get()) {
                BlockPos pos = getSphere(mc.player.getPosition(), obsidianRadius.get(), 6, false, true, 0).stream().filter(this::IsValidBlockPosAnchor).min(Comparator.comparing(blockPos -> getDistanceToBlock(mc.player, blockPos))).orElse(null);
                return pos != null;
            }

            if (mode.getValueByName("Обсидиан").get()) {
                BlockPos pos = getSphere(mc.player.getPosition(), anchorRadius.get(), 6, false, true, 0).stream().filter(this::IsValidBlockPosObisdian).min(Comparator.comparing(blockPos -> getDistanceToBlock(mc.player, blockPos))).orElse(null);
                return pos != null;
            }
            if (mode.getValueByName("Падение").get()) {
                return mc.player.fallDistance >= 30;
            }
        }

        return false;
    }

    public boolean isBall() {
        if (!noBallSwitch.getValue()) {
            return false;
        }
        ItemStack stack = mc.player.getHeldItemOffhand();
        return stack.getDisplayName().getString().toLowerCase().contains("шар") || stack.getDisplayName().getString().toLowerCase().contains("голова") || stack.getDisplayName().getString().toLowerCase().contains("head");
    }

    private int getTotemCount() {
        int count = 0;
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                count++;
            }
        }
        return count;
    }

    private boolean IsValidBlockPosObisdian(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.getBlock().equals(Blocks.OBSIDIAN);
    }

    private boolean IsValidBlockPosAnchor(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.getBlock().equals(Blocks.RESPAWN_ANCHOR);
    }

    private List<BlockPos> getSphere(final BlockPos blockPos, final float radius, final int height, final boolean hollow, final boolean semiHollow, final int yOffset) {
        final ArrayList<BlockPos> spherePositions = new ArrayList<>();
        final int x = blockPos.getX();
        final int y = blockPos.getY();
        final int z = blockPos.getZ();
        final int minX = x - (int) radius;
        final int maxX = x + (int) radius;
        final int minZ = z - (int) radius;
        final int maxZ = z + (int) radius;

        for (int xPos = minX; xPos <= maxX; ++xPos) {
            for (int zPos = minZ; zPos <= maxZ; ++zPos) {
                final int minY = semiHollow ? (y - (int) radius) : y;
                final int maxY = semiHollow ? (y + (int) radius) : (y + height);
                for (int yPos = minY; yPos < maxY; ++yPos) {
                    final double distance = (x - xPos) * (x - xPos) + (z - zPos) * (z - zPos) + (semiHollow ? ((y - yPos) * (y - yPos)) : 0);
                    if (distance < radius * radius && (!hollow || distance >= (radius - 1.0f) * (radius - 1.0f))) {
                        spherePositions.add(new BlockPos(xPos, yPos + yOffset, zPos));
                    }
                }
            }
        }
        return spherePositions;
    }

    private double getDistanceToBlock(Entity entity, final BlockPos blockPos) {
        return getDistance(entity.getPosX(), entity.getPosY(), entity.getPosZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    private double getDistance(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double x = x1 - x2;
        final double y = y1 - y2;
        final double z = z1 - z2;
        return MathHelper.sqrt(x * x + y * y + z * z);
    }
}