package com.github.artemlv.ewm.category.storage.database;

import com.github.artemlv.ewm.category.model.Category;
import com.github.artemlv.ewm.category.storage.CategoryStorage;
import com.github.artemlv.ewm.exception.type.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
}
