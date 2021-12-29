package com.suisrc.kratos.jabus.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.AllArgsConstructor;

@SuppressWarnings("rawtypes")
@AllArgsConstructor
public class ConsumerM implements ConsumerX {

    private Object object;
    private Method method;
    private Method finallyMethod;

    @Override
    public void accept2(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (finallyMethod == null) {
            method.invoke(object, t);
        } else {
            accept3(t);
        }
    }

    public void accept3(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try {
            method.invoke(object, t);
        } finally {
            int cnt = finallyMethod.getParameterCount();
            if (cnt == 1) {
                finallyMethod.invoke(object, method);
            } else if (cnt == 2) {
                finallyMethod.invoke(object, method, t);
            } else if (cnt == 0) {
                finallyMethod.invoke(object);
            } // else 不可处理
        }
    }

}
