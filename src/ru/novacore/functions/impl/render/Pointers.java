package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import ru.novacore.command.friends.FriendStorage;
import ru.novacore.events.EventDisplay;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.player.MoveUtils;
import ru.novacore.utils.player.PlayerUtils;
import ru.novacore.utils.render.ColorUtils;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import ru.novacore.utils.render.RenderUtils;

import java.awt.*;

@FunctionInfo(name = "Arrows", category = Category.Render)
public class Pointers extends Function {

    public float animationStep;

    private float lastYaw;
    private float lastPitch;

    private float animatedYaw;
    private float animatedPitch;

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.player == null || mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

//        animatedYaw = MathUtil.fast(animatedYaw, (mc.player.moveStrafing) * 10,
//                5);
//        animatedPitch = MathUtil.fast(animatedPitch,
//                (mc.player.moveForward) * 10, 5);

        float size = 70;

        if (mc.currentScreen instanceof InventoryScreen) {
            size += 80;
        }

        if (MoveUtils.isMoving()) {
            size += 10;
        }
        animationStep = MathUtil.fast(animationStep, size, 6);
        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                if (!PlayerUtils.isNameValid(player.getNameClear()) || mc.player == player)
                    continue;

                double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getX();
                double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getZ();

                double cos = MathHelper.cos((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
                double sin = MathHelper.sin((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
                double rotY = -(z * cos - x * sin);
                double rotX = -(x * cos + z * sin);

                float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

                double x2 = animationStep * MathHelper.cos((float) Math.toRadians(angle)) + window.getScaledWidth() / 2f;
                double y2 = animationStep * MathHelper.sin((float) Math.toRadians(angle)) + window.getScaledHeight() / 2f;

//                x2 += animatedYaw;
//                y2 += animatedPitch;

                GlStateManager.pushMatrix();
                GlStateManager.disableBlend();
                GlStateManager.translated(x2, y2, 0);
                GlStateManager.rotatef(angle, 0, 0, 1);

                drawTriangle();

                GlStateManager.enableBlend();
                GlStateManager.popMatrix();
            }
        }
        lastYaw = mc.player.rotationYaw;
        lastPitch = mc.player.rotationPitch;
    }

    public static void drawTriangle() {
        RenderUtils.Render2D.drawImage(new ResourceLocation("novacore/images/triangle.png"), -8.0F, -9.0F, 18.0F, 18.0F, -1);

        GL11.glPushMatrix();
        GL11.glPopMatrix();
    }
}