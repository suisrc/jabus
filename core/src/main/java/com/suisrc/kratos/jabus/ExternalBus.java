package com.suisrc.kratos.jabus;

import java.time.Duration;
import java.util.Optional;

public interface ExternalBus {

    /**
     * 请求-回复模型
     * 发送消息后，等待回执
     * @param topic
     * @param clazz message class, 消息类型
     * @return
     */
    <T> Optional<T> request(String topic, Object args, Class<T> clazz, Duration timeout);

    /**
     * 请求-回复模型， 默认提供1s延迟
     */
    default <T> Optional<T> request(String topic, Object args, Class<T> clazz) {
        return request(topic, args, clazz, Duration.ofSeconds(1L)); // 默认， 延迟一秒执行
    }

    /**
     * 发布订阅模型
     * @param topic
     * @param args
     */
    void publish(String topic, Object args);

    /**
     * 关注模型
     * 发布消息，订阅topic的消费者会接收消息
     * 
     * @param topic
     * @param handler Function, ExternalSub
     * @return
     */
    boolean subscribe(String topic, Object handler);

    /**
     * 订阅模型
     * 发布消息，订阅topic的消费者会接收消息
     * @param topic
     * @param handler Consumer, ExternalSub
     * @return
     */
    boolean subscribeAsync(String topic, Object handler);

    /**
     * 关注模型, 支持队列
     * 发布消息，订阅topic的消费者会接收消息
     * 
     * @param topic
     * @param handler Function, ExternalSub
     * @return
     */
    boolean subscribe(String topic, String queue, Object handler);

    /**
     * 订阅模型， 支持队列
     * 发布消息，订阅topic的消费者会接收消息
     * @param topic
     * @param handler Consumer, ExternalSub
     * @return
     */
    boolean subscribeAsync(String topic, String queue, Object handler);

    /**
     * 取消关注
     * 取消后，对应topic所有订阅者都无法接受消息
     * @param topic
     * @param handler
     * @return
     */
    boolean unsubscribe(String topic, Object handler);
}
