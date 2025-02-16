package ru.novacore.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.math.vector.Vector4f;
import ru.novacore.NovaCore;
import ru.novacore.ui.styles.Style;
import ru.novacore.utils.font.FontManager;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.font.Fonts;

@Getter
public class ThemePanel {
    public float x, y, width, height;

    public Style style;
    public float anim;
    public ThemePanel(Style style) {
        this.style = style;
    }

    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        for (Style style : NovaCore.getInstance().getStyleManager().getStyleList()) {
            anim = MathUtil.lerp(anim,  NovaCore.getInstance().getStyleManager().getCurrentStyle() == style ? 1 : MathUtil.isHovered(mouseX,mouseY, x,y,width,height) ? 0.7f : 0, 5);

            RenderUtils.Render2D.drawVectorRound(x, y, width, height, new Vector4f(4, 4, 0, 0), ColorUtils.rgb(18, 18, 18));
            RenderUtils.Render2D.drawGradientRound(x + width, y, width - 180, height, new Vector4f(0, 4, 0,4), this.style.getFirstColor().getRGB(), this.style.getFirstColor().getRGB(), this.style.getSecondColor().getRGB(), this.style.getSecondColor().getRGB());
            Fonts.interMedium.drawText(stack,this.style.getStyleName(), x + 5, y + 7, -1, 6.5f);
        }
    }
}
