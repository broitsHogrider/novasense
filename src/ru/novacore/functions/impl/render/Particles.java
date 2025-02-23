package ru.novacore.functions.impl.render;

import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.math.BlockPos;
import ru.novacore.NovaCore;
import ru.novacore.events.EventHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import ru.novacore.events.input.EventMotion;
import ru.novacore.events.player.AttackEvent;
import ru.novacore.events.render.EventDisplay;
import ru.novacore.events.render.Render3DPosedEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.impl.combat.AttackAura;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.animations.Animation;
import ru.novacore.utils.animations.Direction;
import ru.novacore.utils.animations.impl.DecelerateAnimation;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

@FunctionInfo(name = "Particles", category = Category.Render)
public class Particles extends Function {

    public static final ModeSetting mode = new ModeSetting("Тип", "SnowFlake", "SnowFlake", "Star", "Heart", "Dollar", "FireFly");
    private final SliderSetting quantity = new SliderSetting("Quantity", 10, 5, 20, 1);
    private static final SliderSetting gravity = new SliderSetting("Gravity strange", 2.5f, 1, 5, 0.5f);
    private final ArrayList<Particle> particles = new ArrayList<>();

    public Particles() {
        addSettings(mode, quantity, gravity);
    }

    @EventHandler
    public void onAttack(AttackEvent eventAttack) {
        if (mc.player == null || mc.world == null) return;

        if (eventAttack.entity instanceof PlayerEntity playerEntity) {
            for (int i = 0; i < quantity.getValue().floatValue(); i++) {
                particles.add(new Particle(new Vector3d(
                        playerEntity.getPosX(),
                        playerEntity.getPosY() + playerEntity.getHeight() / 2f,
                        playerEntity.getPosZ())));
            }
        }
    }

    @EventHandler
    public void onRender(Render3DPosedEvent eventRender) {
        particles.removeIf(particle -> particle.alpha.finished(Direction.BACKWARDS));
        particles.forEach(particle -> renderParticle(eventRender, particle));
    }

    private void renderParticle(Render3DPosedEvent eventRender, Particle particle) {
        particle.render(eventRender.matrixStack);
    }

    private static class Particle {

        private final Animation alpha = new DecelerateAnimation(1500, 255);
        private Vector3d motion = generateRandomMotion();
        private Vector3d pos;

        public Particle(Vector3d vector3d) {
            pos = vector3d;
        }

        private Vector3d generateRandomMotion() {
            double[] pos = {ThreadLocalRandom.current().nextFloat(-0.05f, 0.05f), ThreadLocalRandom.current().nextFloat(0.02f, 0.1f), ThreadLocalRandom.current().nextFloat(-0.05f, 0.05f)};
            return new Vector3d(pos[0], pos[1], pos[2]);
        }

        //ПОЧЕМУ ТЫ НЕ ОСТКАКИВАЕШЬ ????
        public void tick() {
            motion = motion.scale(0.98); // Замедление
            motion = new Vector3d(motion.x, motion.y - 0.003, motion.z); // Гравитация

            Vector3d nextPos = pos.add(motion);

            // Проверка столкновений по Y
            if (!mc.world.getBlockState(new BlockPos(pos.x, pos.y - 0.1, pos.z)).isAir()) {
                motion = new Vector3d(motion.x, -motion.y * 0.85 * 1.1, motion.z); // Отскок с увеличением скорости
            }

            // Проверка столкновений по X
            if (!mc.world.getBlockState(new BlockPos(pos.x + motion.x, pos.y, pos.z)).isAir()) {
                motion = new Vector3d(-motion.x * 0.85 * 1.1, motion.y, motion.z); // Ускоренный отскок
            }

            // Проверка столкновений по Z
            if (!mc.world.getBlockState(new BlockPos(pos.x, pos.y, pos.z + motion.z)).isAir()) {
                motion = new Vector3d(motion.x, motion.y, -motion.z * 0.85 * 1.1);
            } else {
                pos = nextPos;
            }
        }

        public void render(MatrixStack matrixStack) {
            tick();

            Direction direction = !alpha.finished(Direction.FORWARDS) && alpha.getDirection() == Direction.FORWARDS ? Direction.FORWARDS : Direction.BACKWARDS;
            alpha.setDirection(direction);

            double x = pos.x - mc.getRenderManager().info.getProjectedView().x;
            double y = pos.y - mc.getRenderManager().info.getProjectedView().y + 0.01;
            double z = pos.z - mc.getRenderManager().info.getProjectedView().z;

            int color1 = ColorUtils.applyOpacity(ColorUtils.getColor(0), (float) alpha.getOutput());
            int color2 = ColorUtils.applyOpacity(ColorUtils.getColor(90), (float) alpha.getOutput());

            matrixStack.push();
            matrixStack.translate(x, y, z);
            matrixStack.scale(0.005f, 0.005f, 0.005f);
            matrixStack.rotate(mc.getRenderManager().getCameraOrientation());
            RenderUtils.Render2D.drawTexture(matrixStack, new ResourceLocation("novacore/images/particle/" + mode.get().toLowerCase() + ".png"), -25, -25, 50, 50, color1, color1, color2, color2);
            matrixStack.pop();
        }
    }
}
