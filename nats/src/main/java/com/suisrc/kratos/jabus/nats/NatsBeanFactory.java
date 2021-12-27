package com.suisrc.kratos.jabus.nats;

import com.suisrc.kratos.jabus.ExternalBus;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;

@Configuration
public class NatsBeanFactory {

    @Bean
    @ConditionalOnMissingBean
    public ExternalBus natsExternalBus(Connection conn) {
        return new NatsExternalBus(conn);
    }
}
