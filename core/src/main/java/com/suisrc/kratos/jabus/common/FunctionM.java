package com.suisrc.kratos.jabus.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.AllArgsConstructor;

@SuppressWarnings("rawtypes")
@AllArgsConstructor
public class FunctionM implements FunctionX {

    private Object object;
    private Method method;
    private Method finallyMethod;
    
    @Override
    public Object apply2(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (finallyMethod == null) {
            return method.invoke(object, t);
        } else {
            return apply3(t);
        }
    }

    public Object apply3(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object result = null;
        try {
            result = method.invoke(object, t);
        } finally {
            int cnt = finallyMethod.getParameterCount();
            if (cnt == 1) {
                finallyMethod.invoke(object, method);
            } else if (cnt == 2) {
                finallyMethod.invoke(object, method, t);
            }  else if (cnt == 3) {
                finallyMethod.invoke(object, method, t, result);
            } else if (cnt == 0) {
                finallyMethod.invoke(object);
            } // else 不可处理
        }
        return result;
    }
}
