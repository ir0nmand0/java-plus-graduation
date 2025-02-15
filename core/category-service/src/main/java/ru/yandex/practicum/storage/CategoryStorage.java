package ru.yandex.practicum.storage;

import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.model.dto.CategoryDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryStorage {
    Category save(Category category);

    void deleteById(final long id);

    List<Category> getAll(final int from, final int size);

    Optional<Category> getById(final long id);

    Category getByIdOrElseThrow(final long id);

    void existsByIdOrElseThrow(final long id);

    List<Category> getAllByIds(Set<Long> ids);
}
