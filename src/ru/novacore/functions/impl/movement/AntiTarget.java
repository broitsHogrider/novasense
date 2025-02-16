package ru.novacore.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.player.InventoryUtil;

@FunctionInfo(name = "Anti Elytra Target", category = Category.Movement)
public class AntiTarget extends Function {


   private final StopWatch stopWatch = new StopWatch();
   private static final int FIREWORK_DELAY = 250;
   @Override
   public void onEnable() {
      stopWatch.reset();
      super.onEnable();
   }

   @Subscribe
   public void onUpdate(EventUpdate eventUpdate) {
      if (mc.player.isElytraFlying()) {
         mc.player.rotationPitch = -35.0F;
      }

      if (mc.player.isOnGround() && !mc.player.isInLava() && !mc.player.isInWater() && !mc.gameSettings.keyBindJump.isKeyDown()) {
         mc.player.jump();
      } else {
         mc.player.startFallFlying();
         mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
      }

      int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true);
      int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, false);

      if (invSlot != -1 || hbSlot != -1) {
         if (!mc.player.getCooldownTracker().hasCooldown(Items.FIREWORK_ROCKET) && stopWatch.hasElapsed(FIREWORK_DELAY)) {
            InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET, -1);

            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

            stopWatch.reset();
         }
      }
   }


   @Override
   public void onDisable() {
      stopWatch.reset();
      super.onDisable();
   }
}

