package ru.yandex.practicum.request.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.dto.RequestDto;
import ru.yandex.practicum.state.State;

import java.util.List;
import java.util.Set;

public interface PrivateUserRequestApi {
    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    RequestDto create(@PathVariable @Positive final long userId,
                             @RequestParam @Positive final long eventId);

    @GetMapping("/{userId}/requests")
    List<RequestDto> getAll(@PathVariable @Positive final long userId);

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    RequestDto cancel(@PathVariable @Positive final long userId,
                             @PathVariable @Positive final long requestId);

    @GetMapping("/{userId}/requests/events/{eventId}")
    List<RequestDto> findAllByEventId(@PathVariable(required = false) @Positive final long userId,
                                      @PathVariable @Positive long eventId);

    @GetMapping("/{userId}/requests/events/{eventId}/ids")
    List<Request> findAllByIdInAndEventId(@PathVariable(required = false) @Positive long userId,
                                          @RequestParam @NotEmpty Set<Long> ids,
                                          @PathVariable @Positive long eventId);

    @GetMapping("/{userId}/requests/events/{eventId}/count")
    int countByEventIdAndStatus(@PathVariable(required = false) @Positive long userId,
                                @PathVariable @Positive long eventId,
                                @RequestParam State state);

    @PostMapping("/{userId}/requests/events/save")
    @ResponseStatus(HttpStatus.CREATED)
    void saveAll(@PathVariable(required = false) @Positive long userId, @RequestBody @NotEmpty List<Request> requests);
}
