package ru.novacore.events;

import java.lang.reflect.Method;

public class Subscriber {
    public final Object instance;
    public final Method method;

    Subscriber(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
        method.setAccessible(true);
    }
}
