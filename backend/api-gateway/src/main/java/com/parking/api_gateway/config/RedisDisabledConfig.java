package com.parking.api_gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false", matchIfMissing = true)
public class RedisDisabledConfig {
    // This configuration disables Redis autoconfiguration when redis is not enabled
}