package ru.yandex.practicum.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.Compilation;
import ru.yandex.practicum.compilation.model.dto.UpdateCompilationDto;

@Component
public class UpdateCompilaionDtoToCompilationConverter implements Converter<UpdateCompilationDto, Compilation> {
    @Override
    public Compilation convert(final UpdateCompilationDto source) {
        return Compilation.builder()
                .title(source.title())
                .build();
    }
}
