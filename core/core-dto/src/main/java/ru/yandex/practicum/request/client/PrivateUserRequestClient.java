package ru.yandex.practicum.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.request.api.PrivateUserRequestApi;

@FeignClient(name = "request-service", path = "/users")
public interface PrivateUserRequestClient extends PrivateUserRequestApi {
}
