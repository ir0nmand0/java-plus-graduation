package ru.yandex.practicum.event.converter;

import ru.yandex.practicum.category.converter.CategoryToCategoryDto;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.location.converter.LocationToLocationDtoConverter;
import ru.yandex.practicum.user.converter.UserToUserWithoutEmailDtoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EventToEventDtoConverter implements Converter<Event, EventDto> {

    @Override
    public EventDto convert(final Event source) {
        return EventDto.builder()
                .id(source.getId())
                .annotation(source.getAnnotation())
                .confirmedRequests(source.getConfirmedRequests())
                .createdOn(source.getCreatedOn())
                .description(source.getDescription())
                .eventDate(source.getEventDate())
                .paid(source.isPaid())
                .participantLimit(source.getParticipantLimit())
                .publishedOn(source.getPublishedOn())
                .requestModeration(source.isRequestModeration())
                .state(source.getState())
                .title(source.getTitle())
                .views(source.getViews())
                .build();
    }
}
