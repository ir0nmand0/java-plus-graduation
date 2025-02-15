package ru.yandex.practicum.category.converter;

import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.model.dto.CategoryDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CategoryDtoToCategory implements Converter<CategoryDto, Category> {
    @Override
    public Category convert(final CategoryDto source) {
        return Category.builder()
                .id(source.id())
                .name(source.name())
                .build();
    }
}