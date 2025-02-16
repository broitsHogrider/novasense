package ru.novacore.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.ModeSetting;

@FunctionInfo(name = "ElytraBoost", category = Category.Movement)
public class ElytraBoost extends Function {
    public ModeSetting modeSetting = new ModeSetting("Режим", "BravoHVH", "BravoHVH", "ReallyWorld");

    public ElytraBoost() {
        addSettings(modeSetting);
    }

    @Subscribe
    public void onUpdate(EventUpdate eventUpdate) {

    }
}
