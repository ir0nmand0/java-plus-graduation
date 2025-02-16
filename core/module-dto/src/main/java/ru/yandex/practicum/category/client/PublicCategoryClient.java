package ru.yandex.practicum.category.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.category.api.PublicCategoryApi;


@FeignClient(name = "category-service", path = "/categories")
public interface PublicCategoryClient extends PublicCategoryApi {
}
