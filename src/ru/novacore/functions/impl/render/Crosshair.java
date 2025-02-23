package ru.novacore.functions.impl.render;

import ru.novacore.events.EventHandler;
import ru.novacore.events.render.EventDisplay;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.RayTraceResult.Type;

import java.awt.*;

@FunctionInfo(name = "Crosshair", category = Category.Render)
public class Crosshair extends Function {

    private final ModeSetting mode = new ModeSetting("Вид", "Орбиз", "Орбиз", "Класический");

    private final BooleanSetting staticCrosshair = new BooleanSetting("Статический", false);
    private float lastYaw;
    private float lastPitch;

    private float animatedYaw;
    private float animatedPitch;

    private float animation;
    private float animationSize;

    private final int outlineColor = Color.BLACK.getRGB();
    private final int entityColor = Color.RED.getRGB();

    public Crosshair() {
        addSettings(mode, staticCrosshair);
    }

    @EventHandler
    public void onDisplay(EventDisplay e) {
        if (mc.player == null || mc.world == null || e.getType() != EventDisplay.Type.POST) {
            return;
        }

        float x = mc.getMainWindow().getScaledWidth() / 2f;
        float y = mc.getMainWindow().getScaledHeight() / 2f;

        switch (mode.getIndex()) {
            case 0 -> {
                float size = 5;

                animatedYaw = MathUtil.fast(animatedYaw,
                        ((lastYaw - mc.player.rotationYaw) + mc.player.moveStrafing) * size,
                        5);
                animatedPitch = MathUtil.fast(animatedPitch,
                        ((lastPitch - mc.player.rotationPitch) + mc.player.moveForward) * size, 5);
                animation = MathUtil.fast(animation, mc.objectMouseOver.getType() == Type.ENTITY ? 1 : 0, 5);

                int color = ColorUtils.interpolateColor(ColorUtils.getColor(1), ColorUtils.getColor(1), 1 - animation);

                if (!staticCrosshair.get()) {
                    x += animatedYaw;
                    y += animatedPitch;
                }

                animationSize = MathUtil.fast(animationSize, (1 - mc.player.getCooledAttackStrength(1)) * 3, 10);

                float radius = 3 + (staticCrosshair.get() ? 0 : animationSize);
                if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    RenderUtils.Render2D.drawShadowCircle(x, y, radius * 2, ColorUtils.setAlpha(color, 64));
                    RenderUtils.Render2D.drawCircle(x, y, radius, color);
                }
                lastYaw = mc.player.rotationYaw;
                lastPitch = mc.player.rotationPitch;
            }

            case 1 -> {
                if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) return;

                float cooldown = 1 - mc.player.getCooledAttackStrength(0);

                float thickness = 1;
                float length = 3;
                float gap = 2 + 8 * cooldown;

                int color = mc.pointedEntity != null ? entityColor : -1;

                drawOutlined(x - thickness / 2, y - gap - length, thickness, length, color);
                drawOutlined(x - thickness / 2, y + gap, thickness, length, color);

                drawOutlined(x - gap - length, y - thickness / 2, length, thickness, color);
                drawOutlined(x + gap, y - thickness / 2, length, thickness, color);
            }
        }
    }

    private void drawOutlined(
            final float x,
            final float y,
            final float w,
            final float h,
            final int hex
    ) {
        RenderUtils.Render2D.drawRect((float) (x - 0.5), (float) (y - 0.5), w + 1, h + 1, outlineColor); // бля че за хуйня поч его хуярит салат что наделал
        RenderUtils.Render2D.drawRect(x, y, w, h, hex);
    }
}
