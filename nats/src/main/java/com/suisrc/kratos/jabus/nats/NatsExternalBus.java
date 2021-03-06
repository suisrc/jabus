package com.suisrc.kratos.jabus.nats;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import com.alibaba.fastjson.JSON;
import com.suisrc.kratos.jabus.ExternalBus;
import com.suisrc.kratos.jabus.core.ExternalSub;
import com.suisrc.kratos.jabus.manager.ExternalBusManager;
import com.suisrc.kratos.jabus.manager.ExternalBusManagerAware;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.Subscription;

// @Component
public class NatsExternalBus implements ExternalBus, ExternalBusManagerAware {

    // 外部订阅连接器
    protected Connection connection;
    // protected Dispatcher dispatcher

    protected ExternalBusManager manager;

    // 订阅缓存
    protected Map<String, Subscription> subs = new ConcurrentHashMap<>();


    public NatsExternalBus(Connection conn) {
        this.connection = conn;
        // this.dispatcher = conn.createDispatcher()
    }

    // 获取所有的订阅信息
    public Map<String, Subscription> getSubscriptions() {
        return subs;
    }

    @Override
    public void setExternalBusManager(ExternalBusManager manager) {
        this.manager = manager;
    }

    @Override
    public ExternalBusManager getManager() {
        return manager;
    }

    public Dispatcher getDispatcher() {
        return connection.createDispatcher();
    }

    //======================================================================================

    @Override
    public String getTopicName(String name) {
        if (this.manager == null) {
            return name;
        }
        return this.manager.spel("", name, name);
    }

    @Override
    public boolean unsubscribe(String topic0, Object handler) {
        if (handler == null) {
            return false;
        }
        String topic = getTopicName(topic0);
        ExternalSub esub = getExternalSub(handler);
        Subscription sub = subs.get(getSubKey(topic, esub));
        if (sub == null || !sub.isActive()) {
            return false;
        }
        // sub.getDispatcher().unsubscribe(sub)
        connection.closeDispatcher(sub.getDispatcher());
        return true;
    }

    // ====================================================================================================

    @Override
    public <T> Optional<T> request(String topic0, Object message, Class<T> clazz, Duration timeout) {
        String topic = getTopicName(topic0);
        try {
            Message msg = connection.request(topic, parseMessage(message), timeout);
            return Optional.ofNullable(msg).map(Message::getData).map(v -> parseObject(v, clazz));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 请求超时
            return Optional.empty();
        }
    }

    @Override
    public void publish(String topic0, Object args) {
        String topic = getTopicName(topic0);
        connection.publish(topic, parseMessage(args));
    }

    // ====================================================================================================

    @Override
    public boolean subscribe(String topic, Object handler) {
        return subscribe(topic, handler, //
            esub -> apply(esub.getParamClass(), esub.getFunction()), //
            method -> getDispatcher().subscribe(topic, method));
    }

    @Override
    public boolean subscribe(String topic, String queue, Object handler) {
        return subscribe(topic, handler, //
            esub -> apply(esub.getParamClass(), esub.getFunction()), //
            method -> getDispatcher().subscribe(topic, queue, method));
    }

    @Override
    public boolean subscribeAsync(String topic, Object handler) {
        return subscribe(topic, handler, //
            esub -> accept(esub.getParamClass(), esub.getConsumer()), //
            method -> getDispatcher().subscribe(topic, method));
    }

    @Override
    public boolean subscribeAsync(String topic, String queue, Object handler) {
        return subscribe(topic, handler, //
            esub -> accept(esub.getParamClass(), esub.getConsumer()), //
            method -> getDispatcher().subscribe(topic, queue, method));
    }

    // ====================================================================================================
    // ====================================================================================================
    // ====================================================================================================

    protected String getSubKey(String topic, Object handler) {
        return topic + "/" + handler.toString();
    }

    protected byte[] parseMessage(Object obj) {
        if (obj instanceof String) {
            return obj.toString().getBytes();
        } else if (obj instanceof byte[]) {
            return (byte[])obj;
        } else {
            return JSON.toJSONBytes(obj);
        }
    }

    @SuppressWarnings({"unchecked"})
    protected <T> T parseObject(byte[] msg, Class<T> clazz) {
        if (clazz == null || byte[].class == clazz || Object.class == clazz) {
            return (T)msg;
        } else if (String.class == clazz) {
            return (T)new String(msg);
        } else {
            return JSON.parseObject(msg, clazz);
        }
    }

    @SuppressWarnings({"all"})
    protected ExternalSub getExternalSub(Object handler) {
        if (handler instanceof ExternalSub) {
            return ((ExternalSub)handler).setBus(this);
        } else if (handler instanceof Consumer) {
            return new ExternalSub(handler).setBus(this).setConsumer((Consumer)handler);
        }  else if (handler instanceof Function) {
            return new ExternalSub(handler).setBus(this).setFunction((Function)handler);
        } else {
            String msg = String.format("no suppert handler type: %s", handler.getClass().getName());
            throw new RuntimeException(msg);
        }
    }

    protected boolean isEmpty(String str) {
      return str == null || str.trim().isEmpty();
    }

    // ====================================================================================================

    protected boolean subscribe(String topic0, Object handler, Function<ExternalSub, MessageHandler> mhdl, //
        Function<MessageHandler, Subscription> msub) {

        String topic = getTopicName(topic0);
        ExternalSub esub = getExternalSub(handler);
        String key = getSubKey(topic, esub);
        Subscription sub = subs.get(key);
        if (sub != null && sub.isActive()) {
            return true;
        }
        sub = msub.apply(mhdl.apply(esub));
        if (!sub.isActive()) {
            return false;
        }
        subs.put(key, sub);
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected MessageHandler apply(Class param, Function func) {
        return message -> {
            Object result = func.apply(parseObject(message.getData(), param));
            if (result != null && !isEmpty(message.getReplyTo())) { // 消息应答
                connection.publish(message.getReplyTo(), parseMessage(result));
            }
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected MessageHandler accept(Class param, Consumer func) {
        return message -> func.accept(parseObject(message.getData(), param));
    }

}
