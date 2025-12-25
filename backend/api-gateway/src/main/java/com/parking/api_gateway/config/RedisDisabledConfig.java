package com.parking.api_gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration is kept for potential future use but is disabled by default.
 * Redis is now enabled by default via RedisConfig.
 */
@Configuration
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false")
public class RedisDisabledConfig {
    // Redis is enabled by default. Set spring.redis.enabled=false to disable.
}