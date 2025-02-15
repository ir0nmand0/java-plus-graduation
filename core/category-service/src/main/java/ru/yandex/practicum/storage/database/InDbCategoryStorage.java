package ru.yandex.practicum.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.exception.type.NotFoundException;
import ru.yandex.practicum.storage.CategoryStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InDbCategoryStorage implements CategoryStorage {
    private static final String SIMPLE_NAME = Category.class.getSimpleName();
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category save(Category category) {
        final Category categoryInStorage = categoryRepository.save(category);
        log.info("Save {} - {}", SIMPLE_NAME, categoryInStorage);
        return categoryInStorage;
    }

    @Override
    @Transactional
    public void deleteById(final long id) {
        categoryRepository.deleteById(id);
        log.info("Delete {} by id - {}", SIMPLE_NAME, id);
    }

    @Override
    public List<Category> getAll(final int from, final int size) {
        final List<Category> categories = categoryRepository.findAll(PageRequest.of(from, size)).getContent();
        log.info("Getting All {} from - {} size - {}", SIMPLE_NAME, from, size);
        return categories;
    }

    @Override
    public Optional<Category> getById(final long id) {
        final Optional<Category> category = categoryRepository.findById(id);
        log.info("Get Optional<{}> by id - {}", SIMPLE_NAME, id);
        return category;
    }

    @Override
    public Category getByIdOrElseThrow(final long id) {
        return getById(id).orElseThrow(() -> new NotFoundException(SIMPLE_NAME, id));
    }

    @Override
    public void existsByIdOrElseThrow(final long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException(SIMPLE_NAME, id);
        }
    }

    @Override
    public List<Category> getAllByIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        return categoryRepository.findAllById(ids);
    }
}
