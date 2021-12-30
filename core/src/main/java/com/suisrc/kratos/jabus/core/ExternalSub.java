package com.suisrc.kratos.jabus.core;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

import com.suisrc.kratos.jabus.ExternalBus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.jodah.typetools.TypeResolver;

/**
 * Consumer, Function, ExternalSub
 */
@NoArgsConstructor
@Getter @Setter
@SuppressWarnings({ "rawtypes" })
public class ExternalSub {
    private ExternalBus bus;
    private Object owner;
    private Method method;
    private Method finallyMethod;
    private Consumer consumer;
    private Function function;
    private Class<?> paramCls;

    public ExternalSub(Object owner) {
        this.owner = owner;
    }

    public ExternalSub(Object owner, Method method, Method finallyMethod) {
        this.owner = owner;
        this.method = method;
        this.finallyMethod = finallyMethod;
    }

    public ExternalSub setConsumer(Consumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public ExternalSub setFunction(Function function) {
        this.function = function;
        return this;
    }

    public ExternalSub setParamCls(Class<?> paramCls) {
        this.paramCls = paramCls;
        return this;
    }

    public ExternalSub setBus(ExternalBus bus) {
        this.bus = bus;
        return this;
    }

    public Consumer getConsumer() {
        if (consumer != null) {
            return consumer;
        } else if (method != null) {
            return Runnable.build(this);
        } else {
            throw new RuntimeException(String.format("%s: consumer is null", toString()));
        }
    }

    public Function getFunction() {
        if (function != null) {
            return function;
        } else if (method != null) {
            return Runnable.build(this);
        } else {
            throw new RuntimeException(String.format("%s: function is null", toString()));
        }
    }

    public Class<?> getParamClass() {
        if (paramCls != null) {
            return paramCls;
        }
        if (method != null) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1) {
                return params[0];
            }
            throw new RuntimeException(String.format("%s: params count > 1, %d", toString(), params.length));
        }
        if (consumer != null) {
            return TypeResolver.resolveRawArguments(Consumer.class, consumer.getClass())[0];
        }
        if (function != null) {
            return TypeResolver.resolveRawArguments(Function.class, function.getClass())[0];
        }
        throw new RuntimeException(String.format("%s: no handler", toString()));
    }

    @Override
    public String toString() {
        if (method != null) {
            return String.format("method->%s::%s", owner.getClass().getName(), method.getName());
        }
        if (consumer != null) {
            return String.format("consumer->%s", consumer.toString());
        }
        if (function != null) {
            return String.format("function->%s", function.toString());
        }
        return String.format("esub->%s", owner.getClass().getName());
    }

    // =======================================================================================

}
