package com.myfmes.github.fwk.test;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import org.junit.Test;

public class TypeIT {

    @Test
    public void Test1() throws ReflectiveOperationException {
        Consumer<String> c = System.out::println;
        c.accept(getLambdaParameterType(c).toString());
    }

    public static Class<?> getLambdaParameterType(Object lambda) throws ReflectiveOperationException {
        Object constantPool = invoke(lambda.getClass(), "getConstantPool");
        int size = (int) invoke(constantPool, "getSize");
        for (int i = size - 1; i >= 0; --i) {
            try {
                Member member = (Member) invoke(constantPool, "getMethodAt", i);
                if (member instanceof Method && member.getDeclaringClass() != Object.class) {
                    return ((Method) member).getParameterTypes()[0];
                }
            } catch (Exception ignored) {
                // ignored
            }
        }
        throw new NoSuchMethodException();
    }

    public static Object invoke(Object obj, String name, Object... args) throws ReflectiveOperationException {
        Method method = getDeclaredMethod(obj.getClass(), name);
        if (Modifier.isPublic(method.getModifiers())) {
            return method.invoke(obj, args);
        }
        method.setAccessible(true);
        try {
            return method.invoke(obj, args);
        } finally {
            method.setAccessible(false);
        }
    }

    public static Method getDeclaredMethod(Class<?> objc, String name) throws NoSuchMethodException {
        for (Method m : objc.getDeclaredMethods()) {
            System.out.println(m.getName());
            if (name.equals(m.getName())) {
                return m;
            }
        }
        throw new NoSuchMethodException();
    }
}
