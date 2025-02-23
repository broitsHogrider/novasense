package ru.novacore.events.input;


import lombok.*;
import ru.novacore.events.Event;

@Data
@AllArgsConstructor
public class EventKey extends Event {
    int key;
    public boolean isKeyDown(int key) {
        return this.key == key;
    }
}
