package ru.yandex.practicum.request.converter;

import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.dto.RequestDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RequsetToRequestDtoConverter implements Converter<Request, RequestDto> {
    @Override
    public RequestDto convert(final Request source) {
        return RequestDto.builder()
                .id(source.getId())
                .requester(source.getRequesterId())
                .event(source.getEventId())
                .status(source.getStatus())
                .created(source.getCreated())
                .build();
    }
}
