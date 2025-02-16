package ru.yandex.practicum.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.request.api.PrivateUserRequestApi;
import ru.yandex.practicum.entity.Request;
import ru.yandex.practicum.request.model.dto.RequestDto;
import ru.yandex.practicum.service.RequestService;
import ru.yandex.practicum.state.State;

import java.util.List;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateUserRequestController implements PrivateUserRequestApi {
    private static final String SIMPLE_NAME = Request.class.getSimpleName();
    private final RequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto create(@PathVariable @Positive final long userId,
                             @RequestParam @Positive final long eventId) {
        log.info("{} to participate in an event by id - {} by user with id - {}", SIMPLE_NAME, eventId, userId);
        return requestService.create(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<RequestDto> getAll(@PathVariable @Positive final long userId) {
        log.info("{} user participation by id - {}", SIMPLE_NAME, userId);
        return requestService.getAll(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestDto cancel(@PathVariable @Positive final long userId,
                             @PathVariable @Positive final long requestId) {
        log.info("{} to cancel participation by id - {} of user with id - {}", SIMPLE_NAME, requestId, userId);
        return requestService.cancel(userId, requestId);
    }

    @Override
    @GetMapping("/{userId}/requests/events/{eventId}")
    public List<RequestDto> findAllByEventId(@PathVariable(required = false) @Positive final long userId,
                                             @PathVariable @Positive final long eventId) {
        log.info("Getting all {} by event id - {}", SIMPLE_NAME, eventId);
        return requestService.findAllByEventId(eventId);
    }

    @Override
    @GetMapping("/{userId}/requests/events/{eventId}/ids")
    public List<RequestDto> findAllByIdInAndEventId(@PathVariable(required = false) @Positive long userId,
                                                 @RequestParam @NotEmpty final Set<Long> ids,
                                                 @PathVariable @Positive final long eventId) {
        log.info("Getting all {} by ids - {} and event id - {}", SIMPLE_NAME, ids, eventId);
        return requestService.findAllByIdInAndEventId(ids, eventId);
    }

    @Override
    @GetMapping("/{userId}/requests/events/{eventId}/count")
    public int countByEventIdAndStatus(@PathVariable(required = false) @Positive long userId,
                                       @PathVariable @Positive final long eventId,
                                       @RequestParam final State state) {
        log.info("Counting {} by event id - {} and status - {}", SIMPLE_NAME, eventId, state);
        return requestService.countByEventIdAndStatus(eventId, state);
    }

    @Override
    @PostMapping("/{userId}/requests/events/save")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveAll(@PathVariable(required = false) @Positive long userId,
                        @RequestBody @NotEmpty List<RequestDto> requests) {
        log.info("Saving batch of {} - size: {}", SIMPLE_NAME, requests.size());
        requestService.saveAll(requests);
    }
}
