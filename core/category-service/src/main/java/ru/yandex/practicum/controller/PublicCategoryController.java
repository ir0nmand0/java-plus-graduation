package ru.yandex.practicum.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.category.api.PublicCategoryApi;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.model.dto.CategoryDto;
import ru.yandex.practicum.service.CategoryService;

import java.util.List;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController implements PublicCategoryApi {
    private static final String SIMPLE_NAME = Category.class.getSimpleName();
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0") @PositiveOrZero final int from,
                                    @RequestParam(defaultValue = "10") @Positive final int size) {
        log.info("Request for all {} beginning - {} size - {}", SIMPLE_NAME, from, size);

        return categoryService.getAll(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getById(@PathVariable @Positive long catId) {
        log.info("Request to get a {} by id - {}", SIMPLE_NAME, catId);

        return categoryService.getById(catId);
    }

    @Override
    @GetMapping("/ids")
    public List<CategoryDto> getAllByIds(@RequestParam Set<@Positive Long> ids) {
        log.info("Request to get a {} by ids - {}", SIMPLE_NAME, ids);

        return categoryService.getAllByIds(ids);
    }
}
