package ru.novacore.functions.impl.render;

import ru.novacore.events.EventHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import ru.novacore.NovaCore;
import ru.novacore.events.EventSystem;
import ru.novacore.events.player.EventUpdate;
import ru.novacore.events.render.Render3DPosedEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.impl.combat.AttackAura;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.utils.animations.Animation;
import ru.novacore.utils.animations.Direction;
import ru.novacore.utils.animations.impl.DecelerateAnimation;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;

@FunctionInfo(name = "TargetESP", category = Category.Render)
public class TargetESP extends Function {
    private final ModeSetting type = new ModeSetting("Тип", "Квадрат", "Квадрат","Призраки");

    public TargetESP() {
        addSettings(type);
    }


    LivingEntity target = null;

    private final Animation alpha = new DecelerateAnimation(600, 255.0f);

    private double speed;
    private long lastTime = System.currentTimeMillis();
    @EventHandler
    public void onUpdate(EventUpdate eventUpdate) {
        AttackAura aura = NovaCore.getInstance().getFunctionRegistry().getAttackAura();
        if (aura.getTarget() != null) target = aura.getTarget();

        alpha.setDirection(aura.isState() && aura.getTarget() != null ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @EventHandler
    private void onWorldEvent(Render3DPosedEvent e) {
        if (target == null && !type.is("Призрак")) return;

        MatrixStack matrixStack = e.matrixStack;
        Vector3d vector3d = RenderUtils.Render3D.getEntityPosition(target, e.partialTicks);

        float time = (float) ((System.currentTimeMillis() - lastTime) / 1500f + Math.sin((System.currentTimeMillis() - lastTime) / 1500f) / 10f);
        float[] offsets = new float[]{-0.3f * target.getHeight(), 0.3f * target.getHeight()}; // Два призрака с разными высотами
        float radius = target.getWidth() * 1.2f; // Чуть увеличенный радиус

        if (type.is("Призраки") && !alpha.finished(Direction.BACKWARDS)) {
            for (int iteration = 0; iteration < 2; iteration++) { // Только два призрака
                float offset = target.getHeight() / 2f + offsets[iteration];
                float localTime = time * (iteration == 0 ? 1 : -1); // Разные направления вращения
                float localRadius = radius * (1.0f + iteration * 0.2f);
                float waveAmplitude = 0.2f + iteration * 0.1f;

                for (int point = 0; point < 20; ++point) {
                    float i = localTime * 360 + point * 4.5f; // Чуть изменён угол поворота
                    float progress = (i - localTime * 360) / 68.0f;
                    float sizeFactor = 0.5f + progress * 0.3f; // Чуть изменена логика размера
                    float size = 0.8f * sizeFactor;

                    double radians = Math.toRadians(i);
                    double cosPos = Math.cos(radians) * localRadius;
                    double sinPos = Math.sin(radians) * localRadius;

                    double offsetY = offset + Math.sin(radians * (1.5 + iteration * 0.3)) * waveAmplitude;
                    double zWobble = Math.cos(localTime * 1.8 + i / 60.0) * 0.15 * iteration;

                    float hurt = target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0);

                    matrixStack.push();
                    matrixStack.translate(vector3d.x + cosPos, vector3d.y + offsetY, vector3d.z + sinPos + zWobble);
                    matrixStack.rotate(mc.getRenderManager().getCameraOrientation());
                    matrixStack.scale(1.05f, 1.0f, 1.0f);

                    int color = ColorUtils.getColor(point * 3, 1.0f);
                    int lastColor = ColorUtils.interpolateColor(color, ColorUtils.rgb(255, 100, 100), hurt);

                    RenderUtils.Render2D.drawTexture(matrixStack, new ResourceLocation("novacore/images/glow.png"),
                            -size / 2f, -size / 2f, size / 2f, size, size, ColorUtils.setAlpha(lastColor, (int) alpha.getOutput()),
                            ColorUtils.setAlpha(lastColor, (int) alpha.getOutput()), ColorUtils.setAlpha(lastColor, (int) alpha.getOutput()), ColorUtils.setAlpha(lastColor, (int) alpha.getOutput()));
                    matrixStack.pop();
                }
            }
        }

    }

    private long startTime1 = -1; // Переменная для отслеживания времени начала анимации
    @EventHandler
    private void onWorld(Render3DPosedEvent renderWorldLastEvent) {
        MatrixStack matrixStack = renderWorldLastEvent.matrixStack;

        if (mc.world == null && mc.player == null) return;

        if (target != null && type.is("Квадрат") && !alpha.finished(Direction.BACKWARDS)) {
            if (startTime1 == -1) {
                startTime1 = System.currentTimeMillis();
            }

            long elapsedTime = System.currentTimeMillis() - startTime1;
            float scale = Math.min(1.0F, (float) elapsedTime / 500.0F); // Линейное увеличение масштаба

            double sin = Math.sin(System.currentTimeMillis() / 1000.0);

            matrixStack.push();
            ActiveRenderInfo camera = mc.gameRenderer.getActiveRenderInfo();
            matrixStack.translate(
                    target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * renderWorldLastEvent.partialTicks - camera.getProjectedView().x,
                    target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * renderWorldLastEvent.partialTicks - camera.getProjectedView().y + (double) (target.getHeight() / 2.0F),
                    target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * renderWorldLastEvent.partialTicks - camera.getProjectedView().z
            );
            matrixStack.rotate(Vector3f.YP.rotationDegrees(-camera.getYaw()));
            matrixStack.rotate(Vector3f.XP.rotationDegrees(camera.getPitch()));

            matrixStack.scale(-0.13F * scale, -0.13F * scale, -0.13F * scale);

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            matrixStack.rotate(Vector3f.ZP.rotationDegrees((float) (sin * 360) * 1.6f));

            boolean hurt = target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0) != 0;
            
            int alpha = (int) this.alpha.getOutput();
            
            RenderUtils.Render2D.drawTexture(matrixStack, new ResourceLocation("novacore/images/target.png"),
                    -4.5F, -4.5F, 9.0F, 9.0F,!hurt ? ColorUtils.setAlpha(ColorUtils.getColor(0), alpha) : ColorUtils.rgba(220, 10, 10, alpha), !hurt ? ColorUtils.setAlpha(ColorUtils.getColor(90), alpha) : ColorUtils.rgba(220, 10, 10, alpha), !hurt ? ColorUtils.setAlpha(ColorUtils.getColor(180), alpha) : ColorUtils.rgba(220, 10, 10, alpha), !hurt ? ColorUtils.setAlpha(ColorUtils.getColor(270), alpha) : ColorUtils.rgba(220, 10, 10, alpha));

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            matrixStack.pop();
        } else {
            startTime1 = -1;
        }
    }
}
