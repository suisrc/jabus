package com.suisrc.kratos.jabus.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.AllArgsConstructor;

@SuppressWarnings("rawtypes")
@AllArgsConstructor
public class FunctionM implements FunctionX {

    private Object object;
    private Method method;
    
    @Override
    public Object apply2(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return method.invoke(object, t);
    }
    
}
