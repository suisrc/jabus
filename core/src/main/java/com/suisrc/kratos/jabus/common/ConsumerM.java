package com.suisrc.kratos.jabus.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.AllArgsConstructor;

@SuppressWarnings("rawtypes")
@AllArgsConstructor
public class ConsumerM implements ConsumerX {

    private Object object;
    private Method method;

    @Override
    public void accept2(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        method.invoke(object, t);
    }
    
}
