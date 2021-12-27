package com.suisrc.kratos.jabus.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.suisrc.kratos.jabus.ExternalBus;
import com.suisrc.kratos.jabus.ExternalSubscribeHandler;
import com.suisrc.kratos.jabus.ExternalSubscriber;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * 加载所有总线的订阅
 * 
 * @see ExternalSubscriber
 * @see ExternalSubscribeHandler
 */
@Configuration
public class ScanExternalBusManager extends AbstractBusManager implements ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        load(); // 加载所有的外部订阅内容
    }

    /**
     * 获取外部总线控制器
     */
    @Override
    public ExternalBus getExternalBus() {
        return context.getBean(ExternalBus.class);
    }

    /**
     * 加载应用中的所有外部总线内容
     */
    @Override
    public List<Object> getSubscribers() {
        Set<Object> subscribers = new HashSet<>();

        Map<String, ExternalSubscriber> sub1 = context.getBeansOfType(ExternalSubscriber.class);
        subscribers.addAll(sub1.values());
        
        Map<String, ExternalSubscribeHandler> sub2 = context.getBeansOfType(ExternalSubscribeHandler.class);
        for ( ExternalSubscribeHandler sh : sub2.values()) {
            subscribers.addAll(sh.getSubscribers());
        }

        return new ArrayList<>(subscribers);
    }
}
