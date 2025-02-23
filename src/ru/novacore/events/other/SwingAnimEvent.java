package ru.novacore.events.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.novacore.events.Event;

@Data
@AllArgsConstructor
public class SwingAnimEvent extends Event {
    int animation;
}
