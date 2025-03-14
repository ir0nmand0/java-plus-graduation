package ru.yandex.practicum.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.Category;
import ru.yandex.practicum.category.model.dto.CategoryDto;

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