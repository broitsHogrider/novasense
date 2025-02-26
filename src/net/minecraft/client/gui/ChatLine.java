package net.minecraft.client.gui;

import lombok.Getter;
import ru.hogoshi.Animation;

public class ChatLine<T>
{
    private final int updateCounterCreated;
    private final T lineString;
    private final int chatLineID;
    @Getter
    private boolean isClient;
    @Getter
    private Animation slideAnimation;
    public ChatLine(int updatedCounterCreated, T lineString, int chatLineID, boolean isClient)
    {
        this.slideAnimation = new Animation();
        this.lineString = lineString;
        this.updateCounterCreated = updatedCounterCreated;
        this.chatLineID = chatLineID;
        this.isClient = isClient;
    }

    public T getLineString()
    {
        return this.lineString;
    }

    public int getUpdatedCounter()
    {
        return this.updateCounterCreated;
    }

    public int getChatLineID()
    {
        return this.chatLineID;
    }
}
