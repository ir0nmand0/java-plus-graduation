package ru.yandex.practicum.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.Request;
import ru.yandex.practicum.request.model.dto.RequestDto;

@Component
public class RequestDtoToRequestConverter implements Converter<RequestDto, Request> {
    @Override
    public Request convert(final RequestDto source) {
        return Request.builder()
                .id(source.getId())
                .created(source.getCreated())
                .eventId(source.getEvent())
                .requesterId(source.getRequester())
                .status(source.getStatus())
                .build();
    }
}
