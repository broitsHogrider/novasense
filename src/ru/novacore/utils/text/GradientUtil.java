package ru.novacore.utils.text;

import ru.novacore.NovaCore;
import ru.novacore.ui.styles.StyleManager;
import ru.novacore.utils.render.ColorUtils;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class GradientUtil {

    public static StringTextComponent gradient(String message) {
        StringTextComponent text = new StringTextComponent("");
        int messageLength = message.length();

        for (int i = 0; i < messageLength; i++) {
            float gradientFactor = (float) i / (messageLength - 1);
            int interpolatedColor = ColorUtils.getColor((int) (90 * gradientFactor), 1.2f);

            text.append(new StringTextComponent(String.valueOf(message.charAt(i)))
                    .setStyle(Style.EMPTY.setColor(new Color(interpolatedColor))));
        }

        return text;
    }


}
