package ru.yandex.practicum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    // Константы для ключей ответа
    private static final String STATUS_KEY = "status";
    private static final String STATUS_VALUE = "error";
    private static final String MESSAGE_KEY = "message";
    private static final String SERVICE_KEY = "service";
    // Общий шаблон для формирования сообщения, где %s будет заменён на имя сервиса
    private static final String MESSAGE_TEMPLATE = "Сервис %s временно недоступен. Попробуйте позже.";

    @GetMapping("/ewm-service")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> ewmServiceFallback() {
        return buildFallbackResponse("ewm-service");
    }

    @GetMapping("/stats-service")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> statsServiceFallback() {
        return buildFallbackResponse("stats-service");
    }

    /**
     * Формирует стандартный ответ для fallback, используя общий шаблон сообщения.
     *
     * @param service Имя сервиса.
     * @return Map с ключами status, message и service.
     */
    private Map<String, String> buildFallbackResponse(String service) {
        String message = String.format(MESSAGE_TEMPLATE, service);
        return Map.of(
                STATUS_KEY, STATUS_VALUE,
                MESSAGE_KEY, message,
                SERVICE_KEY, service
        );
    }
}
