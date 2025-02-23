package ru.novacore.events.other;

import lombok.AllArgsConstructor;
import ru.novacore.events.CancelEvent;

@AllArgsConstructor
public class InventoryCloseEvent extends CancelEvent {

    public int windowId;

}
