package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;

@FunctionInfo(name = "Item 360", category = Category.Render)
public class Item360 extends Function {

    public static BooleanSetting nulltarget = new BooleanSetting("Если нету таргета", true);

    public static  BooleanSetting left = new BooleanSetting("Левая рука", true);
    public static  BooleanSetting right = new BooleanSetting("Правая рука", false);


    public Item360() {
        addSettings(nulltarget,right,left);
    }
   @Subscribe
    public void onEvent(EventUpdate event) {

    }
}

