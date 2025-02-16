package ru.yandex.practicum.category.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.category.model.dto.CategoryDto;
import ru.yandex.practicum.category.model.dto.CreateCategoryDto;

@RequestMapping("/admin/categories")
public interface AdminCategoryApi {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto create(@Valid @RequestBody CreateCategoryDto createCategoryDto);

    @PatchMapping("/{catId}")
    CategoryDto update(@PathVariable @Positive long catId,
                       @Valid @RequestBody CreateCategoryDto createCategoryDto);

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable @Positive long catId);
}
