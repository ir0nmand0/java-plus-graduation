package ru.yandex.practicum.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.Compilation;
import ru.yandex.practicum.compilation.model.dto.CreateCompilationDto;

@Component
public class CreateCompilaionDtoToCompilationConverter implements Converter<CreateCompilationDto, Compilation> {
    @Override
    public Compilation convert(final CreateCompilationDto source) {
        return Compilation.builder()
                .title(source.title())
                .pinned(source.pinned())
                .build();
    }
}
