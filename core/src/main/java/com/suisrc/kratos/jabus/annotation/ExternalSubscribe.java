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
     * 订阅主题, 默认: MethodName-in-0.destination
     * 
     * #{@xxxx.xxx()} spel语法
     * ${xxxxx} xxxxx 环境变量置换
     * ${#xxxx} jabus.???.topics.xxxx
     * ${##xxx} jabus.???.topics.[method]xxx， 注解专用
     * 
     * 类如：jabus.spring.topic.[method]-in-0.destination = topic.xxx...xxx
     * 配置：以下配置， 产生的效果相同
     * 1. topic.xxx...xxx
     * 2. ${##-in-0?}             注解专用
     * 3. ${##-in-0.destination}  注解专用
     * 4. ${#[method]-in-0?}
     * 5. ${#[method]-in-0.destination}
     * 6. ${jabus.spring.topic.[method]-in-0.destination}
     * 7. #{@environment.getProptery('jabus.spring.topic.[method]-in-0.destination')}
     * 8. $>xxxx                  手动专用 = ${#xxx-out-0?} @since 1.1.1
     * 9. $<xxxx                  手动专用 = ${#xxx-in-0?}  @since 1.1.1
     *    $$   特殊配置， 只能和$<联合使用 = ${#xxx-out-0.group}, 改配置只能在queue中生效
     * 
     * 推荐使用2，4，7；
     * 第7种，spel语法， 可以自定义专用方法处理
     * 第2种，再订阅方法上使用， 可以协同queue关联处理，以规约简化相应的配置
     * 第4种，主要使用再消息推送上，request和publish方法， 以简化相应的操作
     * 第8种，新增一种补偿方式， 简化配置, 注意， 只支持配置总线主题
     * 
     * 注意：在手动订阅场景下，第2种方式无法使用，同时queue无法处理‘?}’结尾的内容
     *       注解订阅时不要使用第8种方式，会影响消息队列默认分组不可用，需要单独指定
     * 
     * 
     * @return
     */
    String topic() default "$<#"; // "${##-in-0?}"

    /**
     * 消息队列, 默认: MethodName-in-0.group
     * 同topic, ${#xxx.group}
     * 
     * ${#$, 标识替换，取topic种的内容， 将x:y种的x替换y
     * ${#$?:Y}, topic中?替换成.group， 注解专用
     * 
     * @return
     */
    String queue() default "$$"; // "${#$?:.group}"

    /**
     * 转发， 配置转发模式， 必须有返回值
     * 同topic
     * 
     * 转发， 只有在方法具有返回值， 同时type=ASYNC有效
     * 
     * @return
     */
    String forward() default "$>#"; // "${##-out-0?}"

    /**
     * 订阅类型
     * 
     * SYNC,  必须有返回值， 返回值为请求-响应模型应答内容
     * ASYNC, 如果有返回值， forward为转发的总线， 如果转发总线为空，不进行转发
     * @return
     */
    SubscribeType type() default SubscribeType.ASYNC;

    /**
     * 清空方法， 每次调用后执行
     * xxx(method, args, result)
     * 3个参数从左到右可以顺序舍弃
     * @return
     */
    String finallyMethod() default "";

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
