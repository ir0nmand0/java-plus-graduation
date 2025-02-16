package ru.yandex.practicum.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.user.api.AdminUserApi;

@FeignClient(name = "user-service", path = "/admin/users")
public interface AdminUserClient extends AdminUserApi {
}
