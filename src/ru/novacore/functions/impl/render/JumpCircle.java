package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import ru.novacore.events.JumpEvent;
import ru.novacore.events.WorldEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.render.ColorUtils;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.concurrent.CopyOnWriteArrayList;

@FunctionInfo(name = "JumpCircle", category = Category.Render)
public class JumpCircle extends Function {

    private final CopyOnWriteArrayList<Circle> circles = new CopyOnWriteArrayList<>();
    private final SliderSetting sizeSlider = new SliderSetting("Размер", 1.0f, 1.0f, 5.0f, 0.1f);

    public JumpCircle() {
        addSettings(sizeSlider);
    }

    @Subscribe
    private void onJump(JumpEvent e) {
        circles.add(new Circle(mc.player.getPositon(mc.getRenderPartialTicks()).add(0, 0.05, 0)));
    }

    private final ResourceLocation circle = new ResourceLocation("novacore/images/circle.png");

    @Subscribe
    private void onRender(WorldEvent e) {

        GlStateManager.pushMatrix();
        GlStateManager.shadeModel(7425);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableCull();
        GlStateManager.translated(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z);

        for (Circle c : circles) {
            mc.getTextureManager().bindTexture(circle);
            if (System.currentTimeMillis() - c.time > 3000) circles.remove(c);
            if (System.currentTimeMillis() - c.time > 2000 && !c.isFading) {
                c.fadeAnimation.animate(1, 0, Easings.SINE_OUT);
                c.isFading = true;
            }

            c.animation.update();
            c.fadeAnimation.update();

            float rad = (float) c.animation.getValue() * sizeSlider.get();
            float alpha = (float) c.fadeAnimation.getValue();

            Vector3d vector3d = c.vector3d;
            vector3d = vector3d.add(-rad / 2f, 0, -rad / 2f);

            buffer.begin(6, DefaultVertexFormats.POSITION_COLOR_TEX);
            int alphaValue = (int) (255 * alpha);

            buffer.pos(vector3d.x, vector3d.y, vector3d.z).color(ColorUtils.setAlpha(ColorUtils.getColor(5), alphaValue)).tex(0, 0).endVertex();
            buffer.pos(vector3d.x + rad, vector3d.y, vector3d.z).color(ColorUtils.setAlpha(ColorUtils.getColor(10), alphaValue)).tex(1, 0).endVertex();
            buffer.pos(vector3d.x + rad, vector3d.y, vector3d.z + rad).color(ColorUtils.setAlpha(ColorUtils.getColor(15), alphaValue)).tex(1, 1).endVertex();
            buffer.pos(vector3d.x, vector3d.y, vector3d.z + rad).color(ColorUtils.setAlpha(ColorUtils.getColor(20), alphaValue)).tex(0, 1).endVertex();
            tessellator.draw();
        }

        GlStateManager.disableBlend();
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableAlphaTest();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    private class Circle {

        private final Vector3d vector3d;
        private final long time;
        private final Animation animation = new Animation();
        private final Animation fadeAnimation = new Animation();
        private boolean isFading;

        public Circle(Vector3d vector3d) {
            this.vector3d = vector3d;
            time = System.currentTimeMillis();
            animation.animate(1, 0.5, Easings.SINE_OUT);
            fadeAnimation.animate(1, 1, Easings.SINE_OUT);
        }
    }
}