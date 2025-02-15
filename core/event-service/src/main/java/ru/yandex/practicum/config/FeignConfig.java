package ru.yandex.practicum.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = {
        "ru.yandex.practicum.location.client",
        "ru.yandex.practicum.event.client",
        "ru.yandex.practicum.user.client",
        "ru.yandex.practicum.request.client",
        "ru.yandex.practicum.category.client",
        "ru.yandex.practicum.client"
})
public class FeignConfig {
}