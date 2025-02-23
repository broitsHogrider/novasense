package ru.novacore.functions.impl.misc;

import ru.novacore.NovaCore;
import ru.novacore.events.EventSystem;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
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
