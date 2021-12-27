package com.suisrc.kratos.jabus.common;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public interface ConsumerX<T> extends Consumer<T> {

    @Override
    default void accept(T t) {
        try {
            accept2(t);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    void accept2(T t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}
