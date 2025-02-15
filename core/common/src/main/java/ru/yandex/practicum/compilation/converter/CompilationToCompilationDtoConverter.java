package ru.yandex.practicum.compilation.converter;

import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.compilation.model.dto.CompilationDto;
import ru.yandex.practicum.event.converter.EventToEventDtoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompilationToCompilationDtoConverter implements Converter<Compilation, CompilationDto> {

    @Override
    public CompilationDto convert(final Compilation source) {
        return CompilationDto.builder()
                .id(source.getId())
                .title(source.getTitle())
                .pinned(source.isPinned())
                .build();
    }
}
