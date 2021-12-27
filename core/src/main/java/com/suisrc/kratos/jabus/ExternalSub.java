package com.suisrc.kratos.jabus;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Function;

import com.suisrc.kratos.jabus.common.ConsumerM;
import com.suisrc.kratos.jabus.common.FunctionM;

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
    private Object owner;
    private Method method;
    private Consumer consumer;
    private Function function;
    private Class<?> paramCls;

    public ExternalSub(Object owner, Method method) {
        this.owner = owner;
        this.method = method;
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

    public Consumer getConsumer() {
        if (this.consumer != null) {
            return this.consumer;
        } else if (this.method != null) {
            return new ConsumerM(this.owner, this.method);
        } else {
            throw new RuntimeException(String.format("%s: consumer is null", toString()));
        }
    }

    public Function getFunction() {
        if (this.function != null) {
            return this.function;
        } else if (this.method != null) {
            return new FunctionM(this.owner, this.method);
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

    @Deprecated // 被TypeResolver直接取代
    protected Class<?> getParamClassBy(Class<?> clazz, Type stype) {
        if (clazz == null || clazz == Object.class) {
            throw new RuntimeException(String.format("%s: no %s impl", toString(), stype.toString()));
        }
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == stype) {
                return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else if (type == stype) { // lambda
                return TypeResolver.resolveRawArguments(stype, clazz)[0];
            }
        }
        return getParamClassBy(clazz.getSuperclass(), stype);
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
