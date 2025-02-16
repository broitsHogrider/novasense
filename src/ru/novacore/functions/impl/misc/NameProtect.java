package ru.novacore.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import ru.novacore.NovaCore;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.StringSetting;
import net.minecraft.client.Minecraft;

@FunctionInfo(name = "NameProtect", category = Category.Misc)
public class NameProtect extends Function {


    public static String getReplaced(String input) {
        if (NovaCore.getInstance() != null && NovaCore.getInstance().getFunctionRegistry().getNameProtect().isState()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), "Protected");
        }
        return input;
    }
}
