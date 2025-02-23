package ru.novacore.events;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//By Chat-GPT
public class EventSystem {
    private static final Map<Class<?>, List<Subscriber>> subscribers = new HashMap<>();

    public static void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];
                subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new Subscriber(listener, method));
            }
        }
    }

    public static void unregister(Object listener) {
        for (List<Subscriber> list : subscribers.values()) {
            list.removeIf(subscriber -> subscriber.instance.equals(listener));
        }
    }

    public static void call(Event event) {
        List<Subscriber> list = subscribers.get(event.getClass());
        if (list != null) {
            for (Subscriber subscriber : new ArrayList<>(list)) {
                try {
                    subscriber.method.invoke(subscriber.instance, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
