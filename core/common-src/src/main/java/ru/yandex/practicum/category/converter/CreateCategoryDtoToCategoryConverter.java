package ru.yandex.practicum.category.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.model.dto.CreateCategoryDto;

@Component
public class CreateCategoryDtoToCategoryConverter implements Converter<CreateCategoryDto, Category> {
    @Override
    public Category convert(final CreateCategoryDto source) {
        return Category.builder()
                .name(source.name())
                .build();
    }
}
