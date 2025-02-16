package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import ru.novacore.events.AttackEvent;
import ru.novacore.events.Render3DPosedEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

@FunctionInfo(name = "Particles", category = Category.Render)
public class Particles extends Function {

    private final ModeSetting setting = new ModeSetting("Вид", "Сердечки", "Сердечки", "Орбизы", "Доллар", "Снежинки");
    private final SliderSetting value = new SliderSetting("Кол-во за удар", 20.0f, 1.0f, 50.0f, 1.0f);
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    public Particles() {
        addSettings(setting, value);
    }

    private boolean isInView(Vector3d pos) {
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x,
                mc.getRenderManager().info.getProjectedView().y,
                mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(new AxisAlignedBB(pos.add(-0.2, -0.2, -0.2), pos.add(0.2, 0.2, 0.2)));
    }

    @Subscribe
    private void onUpdate(AttackEvent e) {
        if (e.entity == mc.player) return;
        if (e.entity instanceof LivingEntity livingEntity) {
            for (int i = 0; i < value.get(); i++) {
                particles.add(new Particle(livingEntity.getPositon(mc.getRenderPartialTicks()).add(0, livingEntity.getHeight() / 2f, 0)));
            }
        }
    }

    @Subscribe
    private void onDisplay(Render3DPosedEvent e) {
        MatrixStack matrixStack = e.getMatrix();
        if (mc.player == null || mc.world == null) {
            return;
        }

        particles.removeIf(p -> System.currentTimeMillis() - p.time > 5000 ||
                mc.player.getPositionVec().distanceTo(p.pos) > 30 ||
                !isInView(p.pos) || !mc.player.canEntityBeSeen(p.pos));

        for (Particle p : particles) {
            p.update();
            renderParticle(matrixStack, p);
        }
    }


    private void renderParticle(MatrixStack matrixStack, Particle p) {
        double x = p.getPos().x - mc.getRenderManager().info.getProjectedView().x;
        double y = p.getPos().y - mc.getRenderManager().info.getProjectedView().y + 0.01;
        double z = p.getPos().z - mc.getRenderManager().info.getProjectedView().z;

        float size = 1 - ((System.currentTimeMillis() - p.time) / 5000f);
        int color1 = ColorUtils.applyOpacity(ColorUtils.getColor(0), (int) ((255 * p.alpha) * size));
        int color2 = ColorUtils.applyOpacity(ColorUtils.getColor(90), (int) ((255 * p.alpha) * size));

        matrixStack.push();
        matrixStack.translate(x, y, z);
        matrixStack.scale(0.0035f, 0.0035f, 0.0035f);
        matrixStack.rotate(mc.getRenderManager().getCameraOrientation());

        String texturePath = switch (setting.get()) {
            case "Сердечки" -> "novacore/images/particle/heart.png";
            case "Снежинки" -> "novacore/images/particle/snowflake.png";
            case "Доллар" -> "novacore/images/particle/dollar.png";
            case "Орбизы" -> "novacore/images/particle/glow.png";
            default -> throw new IllegalStateException("Unexpected value: " + setting.get());
        };

        RenderUtils.Render2D.drawTexture(matrixStack, new ResourceLocation(texturePath), -25, -25, 50, 50, color1, color1, color2, color2);
        matrixStack.pop();
    }

    private static class Particle {
        @Getter
        private Vector3d pos;
        private final Vector3d end;
        private final long time;
        private float alpha;

        public Particle(Vector3d pos) {
            this.pos = pos;
            this.end = pos.add(-ThreadLocalRandom.current().nextFloat(-3, 3), -ThreadLocalRandom.current().nextFloat(-3, 3), -ThreadLocalRandom.current().nextFloat(-3, 3));
            this.time = System.currentTimeMillis();
        }

        public void update() {
            alpha = MathUtil.fast(alpha, 1, 10);
            pos = MathUtil.fast(pos, end, 0.5f);
        }
    }
}
