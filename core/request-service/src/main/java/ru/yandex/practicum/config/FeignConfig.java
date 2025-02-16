package ru.yandex.practicum.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = {
        "ru.yandex.practicum.event.client",
        "ru.yandex.practicum.user.client",
        "ru.yandex.practicum.category.client"
})
public class FeignConfig {
}