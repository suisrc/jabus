package com.suisrc.kratos.jabus.manager;

import java.time.Duration;
import java.util.Optional;

import com.suisrc.kratos.jabus.ExternalBus;

public class ExternalBusNone implements ExternalBus {

    @Override
    public ExternalBusManager getManager() {
        return null;
    }

    @Override
    public String getTopicName(String name) {
        return null;
    }

    @Override
    public <T> Optional<T> request(String topic, Object args, Class<T> clazz, Duration timeout) {
        return Optional.empty();
    }

    @Override
    public void publish(String topic, Object args) {
        // do nothing
    }

    @Override
    public boolean subscribe(String topic, Object handler) {
        return false;
    }

    @Override
    public boolean subscribeAsync(String topic, Object handler) {
        return false;
    }

    @Override
    public boolean subscribe(String topic, String queue, Object handler) {
        return false;
    }

    @Override
    public boolean subscribeAsync(String topic, String queue, Object handler) {
        return false;
    }

    @Override
    public boolean unsubscribe(String topic, Object handler) {
        return false;
    }
    
}
