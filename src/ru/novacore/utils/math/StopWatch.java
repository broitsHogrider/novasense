package ru.novacore.utils.math;

import lombok.Getter;

public class StopWatch {
    @Getter
    public long lastMS = System.currentTimeMillis();
    public void reset() {
        lastMS = System.currentTimeMillis();
    }
    public boolean isReached(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }
    public void setLastMS(long newValue) {
        lastMS = System.currentTimeMillis() + newValue;
    }
    public void setTime(long time) {
        lastMS = time;
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }
    public boolean isRunning() {
        return System.currentTimeMillis() - lastMS <= 0;
    }
    public boolean hasTimeElapsed() {
        return lastMS < System.currentTimeMillis();
    }
    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - this.lastMS > time;
    }
    public boolean hasReached(double delay) {
        return (double)(System.currentTimeMillis() - this.lastMS) >= delay;
    }
    public boolean hasElapsed(int fireworkDelay) {
        return System.currentTimeMillis() - this.lastMS > fireworkDelay;
    }
}
