package ru.novacore.functions.api;

import jdk.jfr.Description;
import ru.novacore.NovaCore;
import ru.novacore.functions.impl.misc.ClientSounds;
import ru.novacore.functions.settings.Setting;
import ru.novacore.utils.client.ClientUtil;
import ru.novacore.utils.client.IMinecraft;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public abstract class Function implements IMinecraft {

    final String name;
    final Category category;

    public boolean state;
    @Setter
    public int bind;
    final List<Setting<?>> settings = new ObjectArrayList<>();

    final Animation animation = new Animation();

    public Function() {
        this.name = getClass().getAnnotation(FunctionInfo.class).name();
        this.category = getClass().getAnnotation(FunctionInfo.class).category();
        this.bind = getClass().getAnnotation(FunctionInfo.class).key();

    }

    public Function(String name) {
        this.name = name;
        this.category = Category.Combat;
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }

    public void onEnable() {
        animation.animate(1, 0.25f, Easings.CIRC_OUT);
        NovaCore.getInstance().getEventBus().register(this);
    }

    public void onDisable() {
        animation.animate(0, 0.25f, Easings.CIRC_OUT);
        NovaCore.getInstance().getEventBus().unregister(this);
    }


    public final void toggle() {
        setState(!state, false);
    }

    public final void setState(boolean newState, boolean config) {
        if (state == newState) {
            return;
        }

        state = newState;

        try {
            if (state) {
                onEnable();
            } else {
                onDisable();
            }
            if (!config) {
                FunctionRegistry functionRegistry = NovaCore.getInstance().getFunctionRegistry();
                ClientSounds clientSounds = functionRegistry.getClientSounds();

                NovaCore.getInstance().getNotificationManager().add("Module " + "'" + name + "'" + " has been " + (state ? TextFormatting.GREEN + " enabled!" : TextFormatting.RED + " disabled!"), "debug", 3);

                if (clientSounds != null && clientSounds.isState()) {
                    //ClientUtil.playSound(Math.max(2 + (!this.state ? -0.25f : 0), 0), 0.3f);
                    ClientUtil.playSound((!this.state ? "disable" : "enable"), 75, false);
                }
            }
        } catch (Exception e) {
            handleException(state ? "onEnable" : "onDisable", e);
        }

    }


    private void handleException(String methodName, Exception e) {
        if (mc.player != null) {
            print("[" + name + "] Произошла ошибка в методе " + TextFormatting.RED + methodName + TextFormatting.WHITE
                    + "() Предоставьте это сообщение разработчику: " + TextFormatting.GRAY + e.getMessage());
            e.printStackTrace();
        } else {
            System.out.println("[" + name + " Error" + methodName + "() Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
