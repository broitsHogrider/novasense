package ru.novacore.functions.impl.render;

import java.util.List;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;

import ru.novacore.events.WorldEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.render.ColorUtils;
import lombok.Getter;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

@FunctionInfo(name = "Trails", category = Category.Render)
public class Trails extends Function {

    @Subscribe
    public void onRender(WorldEvent event) {

            for (PlayerEntity entity : mc.world.getPlayers()) {
            entity.points.removeIf(p -> p.time.isReached(500));
            if (entity instanceof ClientPlayerEntity) {
                if (entity == mc.player && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
                    continue;

                Vector3d player = new Vector3d(
                        MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, event.getPartialTicks()),
                        MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, event.getPartialTicks()),
                        MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, event.getPartialTicks()));

                entity.points.add(new Point(player));
            }
        }

        RenderSystem.pushMatrix();

        Vector3d projection = mc.getRenderManager().info.getProjectedView();
        RenderSystem.translated(-projection.x, -projection.y, -projection.z);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.shadeModel(7425);
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(3);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        // render
        for (Entity entity : mc.world.getAllEntities()) {
            GL11.glBegin(GL11.GL_QUAD_STRIP);

            List<Point> points = entity.points;

            for (Point point : points) {

                float index = points.indexOf(point);

                float alpha = index / points.size();
                ColorUtils.setAlphaColor(ColorUtils.getColor(points.indexOf(point), 2), alpha * 0.5f);
                GL11.glVertex3d(point.getPosition().x, point.getPosition().y, point.getPosition().z);
                GL11.glVertex3d(point.getPosition().x, point.getPosition().y + entity.getHeight(),
                        point.getPosition().z);
            }

            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (Point point : points) {

                float index = points.indexOf(point);

                float alpha = index / points.size();
                ColorUtils.setAlphaColor(ColorUtils.getColor(points.indexOf(point), 2), alpha);
                GL11.glVertex3d(point.getPosition().x, point.getPosition().y, point.getPosition().z);
            }
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (Point point : points) {

                float index = points.indexOf(point);

                float alpha = index / points.size();
                ColorUtils.setAlphaColor(ColorUtils.getColor(points.indexOf(point), 2), alpha);
                GL11.glVertex3d(point.getPosition().x, point.getPosition().y +
                        entity.getHeight(),
                        point.getPosition().z);
            }
            GL11.glEnd();

        }
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
        RenderSystem.popMatrix();
    }

    @Getter
    public static class Point {

        private final Vector3d position;
        private final StopWatch time = new StopWatch();

        public Point(Vector3d position) {
            this.position = position;
        }
    }

}
