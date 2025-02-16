package ru.novacore.functions.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface FunctionInfo {
    String name();

    int key() default 0;
    Category category();
}
