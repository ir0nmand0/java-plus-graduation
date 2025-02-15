package ru.yandex.practicum.category.api;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.category.model.dto.CategoryDto;

import java.util.List;
import java.util.Set;

public interface PublicCategoryApi {
    @GetMapping
    List<CategoryDto> getAll(@RequestParam(defaultValue = "0") @PositiveOrZero final int from,
                             @RequestParam(defaultValue = "10") @Positive final int size);

    @GetMapping("/{catId}")
    CategoryDto getById(@PathVariable @Positive long catId);

    @GetMapping("/ids")
    List<CategoryDto> getAllByIds(@RequestParam Set<@Positive Long> ids);
}