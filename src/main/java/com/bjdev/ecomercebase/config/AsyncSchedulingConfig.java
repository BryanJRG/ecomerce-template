package com.bjdev.ecomercebase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables the @Async listeners (order-oversold notifications) and the @Scheduled jobs
 * (abandoned-cart purge, orphaned stock-reservation release) added for the catalog/cart/checkout
 * module. Nothing else in the project used either annotation before this.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfig {
}
