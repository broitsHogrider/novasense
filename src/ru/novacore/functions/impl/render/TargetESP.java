package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;
import ru.novacore.NovaCore;
import ru.novacore.events.EventDisplay;
import ru.novacore.events.EventUpdate;
import ru.novacore.events.Render3DPosedEvent;
import ru.novacore.events.WorldEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.impl.combat.AttackAura;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.utils.animations.Animation;
import ru.novacore.utils.animations.Direction;
import ru.novacore.utils.animations.impl.DecelerateAnimation;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.projections.ProjectionUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;

import java.awt.*;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;

@FunctionInfo(name = "TargetESP", category = Category.Render)
public class TargetESP extends Function {
    private final ModeSetting type = new ModeSetting("Тип", "Квадрат", "Квадрат","Призраки");

    private final AttackAura attackAura;

    public TargetESP(AttackAura attackAura) {
        this.attackAura = attackAura;
        addSettings(type);
    }

    private final Animation alpha = new DecelerateAnimation(500, 1);

    public static long startTime = System.currentTimeMillis();

    @Subscribe
    public void onUpdate(EventUpdate eventUpdate) {
        alpha.setDirection(attackAura.getTarget() != null ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Subscribe
    private void onWorldEvent(Render3DPosedEvent e) {
        if (type.is("Призраки")) {
            if (alpha.finished(Direction.BACKWARDS)) return;

            LivingEntity target = attackAura.getTarget();
            MatrixStack matrixStack = e.getMatrix();
            Vector3d vector3d = RenderUtils.Render3D.getEntityPosition((Entity) target, e.getPartialTicks());

            float time = (float) ((System.currentTimeMillis() - startTime) / 2000f + Math.sin((System.currentTimeMillis() - startTime) / 2000f) / 10f);
            float[] offsets = new float[]{-0.6f * target.getHeight() / 2f, 0.0f, 0.6f * target.getHeight() / 2f};
            float radius = target.getWidth() * 1.05f;

            for (int iteration = 0; iteration < 3; iteration++) {
                float offset = target.getHeight() / 2f + offsets[iteration];

                float localTime = time + iteration * 0.3f;
                float localRadius = radius * (1.0f + iteration * 0.15f);
                float waveAmplitude = 0.15f + iteration * 0.05f;

                for (float i = localTime * 360; i < localTime * 360 + 68.0f; i += 0.5f) {
                    float progress = (i - localTime * 360) / 68.0f;
                    float sizeFactor = iteration == 0.6 ? 0.6f - progress * 0.3f : 0.3f + progress * 0.3f;
                    float size = 0.55f * sizeFactor;

                    double radians = Math.toRadians(i);
                    double cosPos = Math.cos(radians) * localRadius;
                    double sinPos = Math.sin(radians) * localRadius;

                    double offsetY = offset + Math.sin(radians * (1.2 + iteration * 0.3)) * waveAmplitude;
                    double zWobble = Math.cos(localTime * 2 + i / 50.0) * 0.1 * iteration;

                    boolean hurt = attackAura.getTarget().hurtTime - (attackAura.getTarget().hurtTime != 0 ? mc.timer.renderPartialTicks : 0) != 0;

                    matrixStack.push();
                    matrixStack.translate(vector3d.x + cosPos, vector3d.y + offsetY, vector3d.z + sinPos + zWobble);
                    matrixStack.rotate(mc.getRenderManager().getCameraOrientation());
                    matrixStack.scale(1.0f + 0.05f * iteration, 1.0f, 1.0f); // Легкое масштабирование для каждого призрака

                    RenderUtils.Render2D.drawTexture(matrixStack, new ResourceLocation("novacore/images/glow.png"),
                            -size / 2f, -size / 2f, size / 2f, size, size,
                            !hurt ? ColorUtils.applyOpacity(ColorUtils.getColor(0), (float) (220 * alpha.getOutput())) : ColorUtils.rgba(220, 50, 50, 255),
                            !hurt ? ColorUtils.applyOpacity(ColorUtils.getColor(90), (float) (220 * alpha.getOutput())) : ColorUtils.rgba(220, 50, 50, 255),
                            !hurt ? ColorUtils.applyOpacity(ColorUtils.getColor(180), (float) (220 * alpha.getOutput())) : ColorUtils.rgba(220, 50, 50, 255),
                            !hurt ? ColorUtils.applyOpacity(ColorUtils.getColor(270), (float) (220 * alpha.getOutput())) : ColorUtils.rgba(220, 50, 50, 255)
                    );
                    matrixStack.pop();
                }

                time *= -1;
            }
        }
    }


    private long startTime1 = -1; // Переменная для отслеживания времени начала анимации
    @Subscribe
    private void onWorld(Render3DPosedEvent renderWorldLastEvent) {
        MatrixStack matrixStack = renderWorldLastEvent.getMatrix();
        LivingEntity t = NovaCore.getInstance().getFunctionRegistry().getAttackAura().getTarget();

        assert mc.world != null;

        if (t != null && type.is("Квадрат")) {
            if (startTime1 == -1) {
                startTime1 = System.currentTimeMillis();
            }

            long elapsedTime = System.currentTimeMillis() - startTime1;
            float scale = Math.min(1.0F, (float) elapsedTime / 500.0F); // Линейное увеличение масштаба

            double sin = Math.sin(System.currentTimeMillis() / 1000.0);

            matrixStack.push();
            ActiveRenderInfo camera = mc.gameRenderer.getActiveRenderInfo();
            matrixStack.translate(
                    t.lastTickPosX + (t.getPosX() - t.lastTickPosX) * renderWorldLastEvent.getPartialTicks() - camera.getProjectedView().x,
                    t.lastTickPosY + (t.getPosY() - t.lastTickPosY) * renderWorldLastEvent.getPartialTicks() - camera.getProjectedView().y + (double) (t.getHeight() / 2.0F),
                    t.lastTickPosZ + (t.getPosZ() - t.lastTickPosZ) * renderWorldLastEvent.getPartialTicks() - camera.getProjectedView().z
            );
            matrixStack.rotate(Vector3f.YP.rotationDegrees(-camera.getYaw()));
            matrixStack.rotate(Vector3f.XP.rotationDegrees(camera.getPitch()));

            matrixStack.scale(-0.13F * scale, -0.13F * scale, -0.13F * scale);

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            matrixStack.rotate(Vector3f.ZP.rotationDegrees((float) (sin * 360) * 1.6f));

            boolean hurt = t.hurtTime - (t.hurtTime != 0 ? mc.timer.renderPartialTicks : 0) != 0;

            RenderUtils.Render2D.drawTexture(matrixStack, new ResourceLocation("novacore/images/target.png"),
                    -4.5F, -4.5F, 9.0F, 9.0F,!hurt ? ColorUtils.getColor(0) : ColorUtils.rgba(220, 50, 50, 255), !hurt ? ColorUtils.getColor(90) : ColorUtils.rgba(220, 50, 50, 255), !hurt ? ColorUtils.getColor(180) : ColorUtils.rgba(220, 50, 50, 255), !hurt ? ColorUtils.getColor(270) : ColorUtils.rgba(220, 50, 50, 255));

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            matrixStack.pop();
        } else {
            startTime1 = -1;
        }    //dsad
    }
}
