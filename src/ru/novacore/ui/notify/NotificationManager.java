package ru.novacore.ui.notify;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import ru.novacore.utils.animations.Animation;
import ru.novacore.utils.animations.Direction;
import ru.novacore.utils.animations.impl.DecelerateAnimation;
import ru.novacore.utils.client.IMinecraft;
import ru.novacore.utils.font.FontManager;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements IMinecraft {

    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public void add(String text, String content, int time) {
        notifications.add(new Notification(text, content, time));
    }

    public void draw(MatrixStack matrixStack) {
        float yOffset = 0;
        for (Notification notification : notifications) {
            notification.animation.setDirection(notification.getTimerUtils().hasTimeElapsed(notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (notification.animation.finished(Direction.BACKWARDS)) {
                notifications.remove(notification);
                continue;
            }

            float x = (float) (window.getScaledWidth() - (notification.width + 5) * notification.animation.getOutput());
            float y = (float) (window.getScaledHeight() - (yOffset + notification.height + 6 + 14));
            notification.setX(x);
            notification.setY(y);
            notification.draw(matrixStack);
            yOffset += (float) ((notification.height - 11) * notification.animation.getOutput());
        }
    }

    private static class Notification {

        @Setter
        private float x, y;

        @Getter
        private long time;

        @Getter
        private final StopWatch timerUtils = new StopWatch();

        private final Animation animation = new DecelerateAnimation(300, 1);
        private final String text, content;
        private float width, height;

        public Notification(String text, String content, int time) {
            this.text = text;
            this.content = content;
            this.time = time * 1000L;
        }

        public void draw(MatrixStack matrixStack) {
            width = FontManager.sfBold[15].getWidth(text) + 8;
            height = 27;

            RenderUtils.Render2D.drawRound(x, y + 11.5f, width, height - 13, 5, ColorUtils.rgba(20, 20, 20, 200));
            FontManager.sfBold[15].drawString(matrixStack, text, x + 4, y + height / 2f + 3, -1);
        }
    }
}