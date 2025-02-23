package ru.novacore.functions.impl.combat;

import ru.novacore.events.EventHandler;
import com.ibm.icu.impl.Pair;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import ru.novacore.events.input.EventKey;
import ru.novacore.events.input.EventMouseButtonPress;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BindSetting;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.utils.player.InventoryUtil;

@FunctionInfo(name = "AutoSwap", category = Category.Combat)
public class AutoSwap extends Function {

    private final BindSetting key = new BindSetting("Свап", -1);
    
    private final ModeSetting mode = new ModeSetting("Что на что?","Шар & Щит", "Шар & Щит", "Тотем & Шар", "Щит & Тотем", "Шар & Гепл", "Тотем & Гепл", "Щит & Гепл");

    private Pair<Item, Item> getSwapPair() {
        if (mode.is("Шар & Щит")) {
            return Pair.of(Items.PLAYER_HEAD, Items.SHIELD);
        }
        if (mode.is("Тотем & Шар")) {
            return Pair.of(Items.TOTEM_OF_UNDYING, Items.PLAYER_HEAD);
        }
        if (mode.is("Щит & Тотем")) {
            return Pair.of(Items.SHIELD, Items.TOTEM_OF_UNDYING);
        }
        if (mode.is("Шар & Гепл")) {
            return Pair.of(Items.PLAYER_HEAD, Items.GOLDEN_APPLE);
        }
        if (mode.is("Тотем & Гепл")) {
            return Pair.of(Items.TOTEM_OF_UNDYING, Items.GOLDEN_APPLE);
        }
        if (mode.is("Щит & Гепл")) {
            return Pair.of(Items.SHIELD, Items.GOLDEN_APPLE);
        }
        return Pair.of(Items.AIR, Items.AIR);
    }

    public AutoSwap() {
        addSettings(key, mode);
    }

    @EventHandler
    private void onMouse(EventMouseButtonPress eventMouseButtonPress) {
        if (eventMouseButtonPress.getButton() == key.get()) spaw();
    }
    @EventHandler
    private void onKeyPress(EventKey eventKey) {
        if (eventKey.getKey() == key.get()) spaw();
    }

    private void spaw() {
        boolean handEmpty = (mc.player.getHeldItemOffhand().getItem() instanceof AirItem);

        int firstItemSlot = InventoryUtil.getItemSlot(getSwapPair().first);
        int secondItemSlot = InventoryUtil.getItemSlot(getSwapPair().second);

        int toSwapSlot = 45;
        if (firstItemSlot != -1 || secondItemSlot != -1) {
            if (secondItemSlot == -1) {
                mc.playerController.windowClick(0, firstItemSlot, 0, ClickType.PICKUP, mc.player);
            } else {
                mc.playerController.windowClick(0, secondItemSlot, 0, ClickType.PICKUP, mc.player);
            }
            mc.playerController.windowClick(0, toSwapSlot, 0, ClickType.PICKUP, mc.player);
            if (!handEmpty) {
                if (secondItemSlot == -1) {
                    mc.playerController.windowClick(0, firstItemSlot, 0, ClickType.PICKUP, mc.player);
                } else {
                    mc.playerController.windowClick(0, secondItemSlot, 0, ClickType.PICKUP, mc.player);
                }
            }
        }
    }
}