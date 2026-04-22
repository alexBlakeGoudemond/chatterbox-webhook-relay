package com.webhook.relay.chatterbox.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.webhook.relay.chatterbox.common.config.properties.concurrency.ExecutorProperties;

@Configuration
public class ExecutorPropertiesConfig {

    @Bean
    @ConfigurationProperties("chatterbox.polled-event-executor")
    public ExecutorProperties polledEventExecutorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    @ConfigurationProperties("chatterbox.webhook-event-executor")
    public ExecutorProperties webhookEventExecutorProperties() {
        return new ExecutorProperties();
    }

}
