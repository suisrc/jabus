package com.suisrc.kratos.jabus.nats;

import com.suisrc.kratos.jabus.ExternalBus;
import com.suisrc.kratos.jabus.manager.ExternalBusNone;
import com.suisrc.kratos.jabus.manager.ScanExternalBusManager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;

@Configuration
public class NatsBeanFactory {

    @Bean
    @ConditionalOnMissingBean
    public ExternalBus natsExternalBus(ApplicationContext applicationContext) {
        boolean enabled = applicationContext.getEnvironment().getProperty(//
            ScanExternalBusManager.PREFIX + ".enabled", Boolean.class, true);
        if (!enabled) {
            return new ExternalBusNone();
        }
        Connection conn = applicationContext.getBean(Connection.class);
        return new NatsExternalBus(conn);
    }
}
