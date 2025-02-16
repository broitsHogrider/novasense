package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ru.novacore.command.friends.FriendStorage;
import ru.novacore.events.WorldEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@FunctionInfo(name = "China Hat", category = Category.Render)
public class ChinaHat extends Function {

    private final BooleanSetting friendHat = new BooleanSetting("У друзей", true);

    public ChinaHat() {
        addSettings(friendHat);
    }

    @Subscribe
    private void onRender(WorldEvent e) {
        if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) {
            renderHatForPlayer(mc.player, e);
        }

        if (friendHat.get()) {
            for (PlayerEntity friend : mc.world.getPlayers()) {
                if (FriendStorage.isFriend(friend.getName().getString()) && friend != mc.player) {
                    renderHatForPlayer(friend, e);
                }
            }
        }
    }

    private void renderHatForPlayer(PlayerEntity player, WorldEvent e) {
        float radius = 0.6f;
        float heightOffset = 0.0f;

        ItemStack headItem = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (!headItem.isEmpty() && (headItem.getItem() == Items.IRON_HELMET || headItem.getItem() == Items.DIAMOND_HELMET || headItem.getItem() == Items.GOLDEN_HELMET || headItem.getItem() == Items.LEATHER_HELMET || headItem.getItem() == Items.NETHERITE_HELMET)) {
            heightOffset = 0.11f;
        }

        GlStateManager.pushMatrix();

        RenderSystem.translated(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z);
        Vector3d interpolated = MathUtil.interpolate(player.getPositionVec(), new Vector3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ), e.getPartialTicks());
        interpolated.y -= 0.05f;
        RenderSystem.translated(interpolated.x, interpolated.y + player.getHeight() + heightOffset, interpolated.z);
        final double pitch = mc.getRenderManager().info.getPitch();
        final double yaw = mc.getRenderManager().info.getYaw();

        GL11.glRotatef((float) -yaw, 0f, 1f, 0f);

        RenderSystem.translated(-interpolated.x, -(interpolated.y + player.getHeight() + heightOffset), -interpolated.z);

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.shadeModel(7425);
        RenderSystem.lineWidth(3);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(interpolated.x, interpolated.y + player.getHeight() + 0.3 + heightOffset, interpolated.z).color(ColorUtils.setAlpha(ColorUtils.getColor(0, 1), 128)).endVertex();
        for (int i = 0; i <= 360; i++) {
            float x = (float) (interpolated.x + MathHelper.sin((float) Math.toRadians(i)) * radius);
            float z = (float) (interpolated.z + -MathHelper.cos((float) Math.toRadians(i)) * radius);

            buffer.pos(x, interpolated.y + player.getHeight() + heightOffset, z).color(ColorUtils.setAlpha(ColorUtils.getColor(i, 1), 128)).endVertex();
        }
        tessellator.draw();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i++) {
            float x = (float) (interpolated.x + MathHelper.sin((float) Math.toRadians(i)) * radius);
            float z = (float) (interpolated.z + -MathHelper.cos((float) Math.toRadians(i)) * radius);

            buffer.pos(x, interpolated.y + player.getHeight() + heightOffset, z).color(ColorUtils.setAlpha(ColorUtils.getColor(i, 1), 255)).endVertex();
        }
        tessellator.draw();
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.shadeModel(7424);
        GlStateManager.popMatrix();
    }
}