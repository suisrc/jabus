package com.suisrc.kratos.jabus.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import com.suisrc.kratos.jabus.ExternalBus;
import com.suisrc.kratos.jabus.annotation.ExternalSubscribe;
import com.suisrc.kratos.jabus.annotation.ExternalSubscribe.SubscribeType;
import com.suisrc.kratos.jabus.common.ConsumerX;
import com.suisrc.kratos.jabus.common.FunctionX;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

@Log
@SuppressWarnings("rawtypes")
@AllArgsConstructor
public class Runnable implements FunctionX, ConsumerX {

    @Override
    public void accept2(Object args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        execute.exec(args, null);
    }

    @Override
    public Object apply2(Object args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return execute.exec(args, null);
    }

    private Execute execute;
    //=====================================================================================================================

    public static Runnable build(ExternalSub sub) {
        Object object = sub.getOwner();
        Method method = sub.getMethod();
        Execute execute = (args, r) -> method.invoke(object, args); // r 无视， 执行订阅方法

        ExternalSubscribe subscribe = method.getAnnotation(ExternalSubscribe.class);
        if (subscribe != null && subscribe.type() == SubscribeType.ASYNC && // 
            !subscribe.forward().isEmpty() && method.getReturnType() != void.class) {

            String forward = sub.getBus().getManager().spel(method.getName(), subscribe.topic(), subscribe.forward());
            if (forward.isEmpty()) {
                throw new RuntimeException(String.format("external subscribe method error, forward is empty: %s::%s", //
                    method.getDeclaringClass().getSimpleName(), method.getName()));
            }
            log.log(Level.INFO, "加载外部订阅器转发：主题[{0}], 方法[{1}::{2}]", new Object[]{
                forward, object.getClass().getName(), method.getName()
            });
            execute = execute.after(new ForwardTopic(sub.getBus(), forward));
        }

        if (sub.getFinallyMethod() != null) {
            Method fmethod = sub.getFinallyMethod();
            execute = execute.finall1(new FinallyMethod(object, fmethod, fmethod.getParameterCount()));
        }

        return new Runnable(execute);
    }
    //=====================================================================================================================

    @AllArgsConstructor
    private static class FinallyMethod implements Execute {
        private Object object;
        private Method method;
        private int paramCnt;
        @Override
        public Object exec(Object t, Object r) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (paramCnt == 0) {
                method.invoke(object);
            } else if (paramCnt == 1) {
                method.invoke(object, method);
            } else if (paramCnt == 2) {
                method.invoke(object, method, t);
            }  else if (paramCnt == 3) {
                method.invoke(object, method, t, r);
            }// else 不可处理
            return r;
        }
    }
    //=====================================================================================================================

    @AllArgsConstructor
    private static class ForwardTopic implements Execute {
        private final ExternalBus bus;
        private final String forward;
        @Override
        public Object exec(Object t, Object r) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (r != null) {
                 bus.publish(forward, r);
            }
            return r;
        }
    }

}
