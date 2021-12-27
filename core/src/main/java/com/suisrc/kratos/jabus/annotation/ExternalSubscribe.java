package com.suisrc.kratos.jabus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> 外部消息订阅
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExternalSubscribe {

    /**
     * 订阅主题
     * @return
     */
    String topic() default "";

    /**
     * 消息队列
     * @return
     */
    String queue() default "";

    /**
     * 订阅类型
     * @return
     */
    SubscribeType type() default SubscribeType.ASYNC;

    /**
     * 订阅类型
     * 
     * 同步订阅， 需要有返回值
     * 异步订阅， 无返回值
     */
    enum SubscribeType {
        NONE, SYNC, ASYNC;
    }
}
