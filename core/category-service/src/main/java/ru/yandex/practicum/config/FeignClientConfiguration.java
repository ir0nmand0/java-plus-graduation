package ru.yandex.practicum.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Feign клиентов для основного приложения
 */
@Configuration
@EnableFeignClients(basePackages = "ru.yandex.practicum.event.client")
public class FeignClientConfiguration {
}