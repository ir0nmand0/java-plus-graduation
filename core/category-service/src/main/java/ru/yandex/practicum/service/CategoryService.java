package ru.yandex.practicum.service;

import ru.yandex.practicum.category.model.dto.CategoryDto;
import ru.yandex.practicum.category.model.dto.CreateCategoryDto;

import java.util.List;
import java.util.Set;

public interface CategoryService {

    CategoryDto create(final CreateCategoryDto createCategoryDto);

    CategoryDto update(final CreateCategoryDto createCategoryDto, final long id);

    void deleteById(final long id);

    List<CategoryDto> getAll(final int from, final int size);

    CategoryDto getById(final long id);

    List<CategoryDto> getAllByIds(Set<Long> ids);
}
