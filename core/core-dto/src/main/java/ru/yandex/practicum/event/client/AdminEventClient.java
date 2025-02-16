package ru.yandex.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.event.api.AdminEventApi;

@FeignClient(name = "event-service", path = "/admin/events")
public interface AdminEventClient extends AdminEventApi {
}
