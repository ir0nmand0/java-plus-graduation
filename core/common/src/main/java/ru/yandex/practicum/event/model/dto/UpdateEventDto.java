package ru.yandex.practicum.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.event.validation.ConstraintFutureInTwoHours;
import ru.yandex.practicum.event.validation.ConstraintPositiveOrZero;
import ru.yandex.practicum.location.model.dto.LocationLatAndLonDto;
import ru.yandex.practicum.state.StateAction;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

public record UpdateEventDto(
        @Length(min = 20, max = 2000)
        String annotation,
        long category,
        @Length(min = 20, max = 7000)
        String description,
        @ConstraintFutureInTwoHours
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,
        LocationLatAndLonDto location,
        Boolean paid,
        @ConstraintPositiveOrZero
        Integer participantLimit,
        boolean requestModeration,
        StateAction stateAction,
        @Length(min = 3, max = 120)
        String title
) {
}
