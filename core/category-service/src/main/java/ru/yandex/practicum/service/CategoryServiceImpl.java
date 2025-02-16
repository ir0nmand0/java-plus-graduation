package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.entity.Category;
import ru.yandex.practicum.category.model.dto.CategoryDto;
import ru.yandex.practicum.category.model.dto.CreateCategoryDto;
import ru.yandex.practicum.event.client.AdminEventClient;
import ru.yandex.practicum.exception.type.ConflictException;
import ru.yandex.practicum.storage.CategoryStorage;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    @Qualifier("mvcConversionService")
    private final ConversionService cs;
    private final CategoryStorage categoryStorage;
    private final AdminEventClient adminEventClient;

    @Override
    public CategoryDto create(CreateCategoryDto createCategoryDto) {
        Category category = cs.convert(createCategoryDto, Category.class);
        return cs.convert(categoryStorage.save(category), CategoryDto.class);
    }

    @Override
    public CategoryDto update(final CreateCategoryDto createCategoryDto, final long id) {
        Category category = categoryStorage.getByIdOrElseThrow(id);
        category.setName(createCategoryDto.name());
        log.info("Update category - {}", category);

        return cs.convert(categoryStorage.save(category), CategoryDto.class);
    }

    @Override
    public void deleteById(final long id) {
        if (adminEventClient.existsByCategoryId(id)) {
            throw new ConflictException("Category with id " + id + " exists in Event");
        }

        categoryStorage.existsByIdOrElseThrow(id);

        categoryStorage.deleteById(id);
    }

    @Override
    public List<CategoryDto> getAll(final int from, final int size) {
        return categoryStorage.getAll(from, size).stream()
                .map(category -> cs.convert(category, CategoryDto.class))
                .toList();
    }

    @Override
    public CategoryDto getById(final long id) {
        return cs.convert(categoryStorage.getByIdOrElseThrow(id), CategoryDto.class);
    }

    @Override
    public List<CategoryDto> getAllByIds(Set<Long> ids) {
        return categoryStorage.getAllByIds(ids).stream()
                .map(category -> cs.convert(category, CategoryDto.class))
                .toList();
    }
}
