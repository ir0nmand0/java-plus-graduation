package ru.yandex.practicum.event.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.dto.EventDto;

@Component
public class EventDtoToEventConverter implements Converter<EventDto, Event> {

    @Override
    public Event convert(final EventDto source) {
        return Event.builder()
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
                // Извлечение идентификаторов из вложенных DTO:
                .categoryId(source.getCategory() != null ? source.getCategory().id() : null)
                .initiatorId(source.getInitiator() != null ? source.getInitiator().id() : null)
                .locationId(source.getLocation() != null ? source.getLocation().id() : null)
                .build();
    }
}
