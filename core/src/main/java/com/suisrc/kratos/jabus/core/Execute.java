package com.suisrc.kratos.jabus.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

@FunctionalInterface
public interface Execute {

    Object exec(Object t, Object r) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    
    default Execute before(Execute before) {
        Objects.requireNonNull(before);
        return (t, r) -> exec(t, before.exec(t, r));
    }

    default Execute after(Execute after) {
        Objects.requireNonNull(after);
        return (t, r) -> after.exec(t, exec(t, r));
    }

    default Execute finall1(Execute finall) { // finally
        Objects.requireNonNull(finall);
        return (t, r) -> { try { r = exec(t, r); } finally { r = finall.exec(t, r); } return r; };
    }

    default Execute finall2(Execute func) { // this is finally
        Objects.requireNonNull(func);
        return (t, r) -> { try { r = func.exec(t, r); } finally { r = exec(t, r); } return r; };
    }
}
