package ru.novacore.functions.settings;

import java.util.function.Supplier;

public class Setting<Value> implements ISetting {

    Value defaultVal;

    String settingName;
    public Supplier<Boolean> visible = () -> true;

    public Setting(String name, Value defaultVal) {
        this.settingName = name;
        this.defaultVal = defaultVal;
    }


    public String getName() {
        return settingName;
    }

    public void set(Value value) {
        defaultVal = value;
    }

    @Override
    public Setting<?> setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    public Boolean visible() {
        return visible.get();
    }

    public Value get() {
        return defaultVal;
    }

    public Value getValue() {
        return defaultVal;
    }
}