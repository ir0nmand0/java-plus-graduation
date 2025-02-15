package ru.yandex.practicum.location.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.location.api.AdminLocationApi;


@FeignClient(name = "location-service", path = "/admin/locations")
public interface AdminLocationClient extends AdminLocationApi {
}
