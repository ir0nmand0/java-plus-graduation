package com.github.artemlv.ewm.event.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.github.artemlv.ewm.event.client")
public class FeignConfig {
}