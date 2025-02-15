package ru.yandex.practicum.event.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.model.dto.CreateEventDto;
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.event.model.dto.UpdateEventDto;
import ru.yandex.practicum.request.model.dto.RequestDto;
import ru.yandex.practicum.request.model.dto.RequestStatusUpdateResultDto;
import ru.yandex.practicum.request.model.dto.UpdateRequestByIdsDto;

import java.util.List;

@RequestMapping("/users/{userId}/events")
public interface PrivateEventApi {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
     EventDto create(@RequestBody @Valid final CreateEventDto createEventDto,
                           @PathVariable @Positive final long userId);

    @GetMapping
     List<EventDto> getAllByUserId(@PathVariable @Positive final long userId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
                                         @RequestParam(defaultValue = "10") @Positive final int size);

    @GetMapping("/{eventId}")
     EventDto getByIdAndUserId(@PathVariable @Positive final long userId,
                                     @PathVariable @Positive final long eventId);

    @GetMapping("/{eventId}/requests")
     List<RequestDto> getRequestsByUserIdAndEventId(@PathVariable @Positive final long userId,
                                                          @PathVariable @Positive final long eventId);

    @PatchMapping("/{eventId}/requests")
     RequestStatusUpdateResultDto updateRequestsByUserAndEvent(
            @RequestBody final UpdateRequestByIdsDto updateEventStatusByRequestIds,
            @PathVariable @Positive final long userId,
            @PathVariable @Positive final long eventId);

    @PatchMapping("/{eventId}")
     EventDto update(@PathVariable @Positive final long userId,
                           @PathVariable @Positive final long eventId,
                           @RequestBody @Valid UpdateEventDto updateEventDto);
}
