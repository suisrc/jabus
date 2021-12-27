package com.suisrc.kratos.jabus.common;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public interface FunctionX<T, R> extends Function<T, R> {

    @Override
    default R apply(T t) {
        try {
            return apply2(t);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    R apply2(T t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}
