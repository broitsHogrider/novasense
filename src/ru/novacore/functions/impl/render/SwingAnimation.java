package ru.novacore.functions.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.HandSide;
import ru.novacore.NovaCore;
import ru.novacore.events.EventHandler;
import ru.novacore.events.other.SwingAnimEvent;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.impl.combat.AttackAura;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.functions.settings.impl.SliderSetting;

@Getter
@FunctionInfo(name = "SwingAnimation", category = Category.Render)
public class SwingAnimation extends Function {


    private final BooleanSetting translate = new BooleanSetting("Оффсеты рук",  true);
    private final SliderSetting offsetX = new SliderSetting("Дистанция по X",  0.0F, -1F, 1F, 0.05F).setVisible(() -> translate.getValue());
    private final SliderSetting offsetY = new SliderSetting("Дистанция по Y",  0.0F, -0.5F, 1F, 0.05F).setVisible(() -> translate.getValue());
    private final SliderSetting offsetZ = new SliderSetting("Дистанция по Z", 0.0F, -1F, 0F, 0.05F).setVisible(() -> translate.getValue());
    private final BooleanSetting auraOnly = new BooleanSetting("Только при киллке",  false);
    public final ModeSetting swingMode = new ModeSetting("Анимации","Без анимации",
                    "Swipe",
                    "Swipe Back",
                    "Smooth Old",
                    "Smooth New",
                    "Slap",
                    "DeadCode",
                    "Knife",
                    "Lower Power",
                    "Pinch",
                    "Knock",
                    "Surf",
                    "Destroy",

                    "Back Feast",
                    "Без анимации");

    private final SliderSetting speed = new SliderSetting("Скорость анимации",  1F, 0.5F, 3.0F, 0.05F);

    public SwingAnimation() {
        addSettings(translate, offsetX, offsetY, offsetZ, auraOnly, swingMode, speed);
    }

    public boolean auraCheck() {
        AttackAura aura = NovaCore.getInstance().getFunctionRegistry().getAttackAura();
        return !auraOnly.getValue() || aura.isState() && aura.getTarget() != null;
    }

    @EventHandler
    private void onSwingEvent(SwingAnimEvent event) {
        event.setAnimation((int) (event.getAnimation() * speed.get().floatValue()));
    }

//    private final Listener<SwingAnimationEvent> onSwing = event ->
//            event.setAnimation((int) (event.getAnimation() * speed.getValue().floatValue()));
//    private final Listener<RenderItemEvent> onRenderItem = event -> {
//        boolean rightHand = event.getHandSide().equals(HandSide.RIGHT);
//        MatrixStack matrix = event.getMatrix();
//        if (translate.getValue()) {
//            if (rightHand) {
//                matrix.translate(
//                        offsetX.getValue().floatValue(),
//                        offsetY.getValue().floatValue(),
//                        offsetZ.getValue().floatValue()
//                );
//            } else {
//                matrix.translate(
//                        -offsetX.getValue().floatValue(),
//                        offsetY.getValue().floatValue(),
//                        offsetZ.getValue().floatValue()
//                );
//
//            }
//        }
//    };

}
