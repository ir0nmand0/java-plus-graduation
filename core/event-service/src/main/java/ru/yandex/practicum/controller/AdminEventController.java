package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.api.AdminEventApi;
import ru.yandex.practicum.event.model.dto.AdminParameterDto;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.event.model.dto.UpdateEventDto;
import ru.yandex.practicum.service.EventService;

import java.util.List;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController implements AdminEventApi {
    private static final String SIMPLE_NAME = Event.class.getSimpleName();
    private final EventService eventService;

    @GetMapping
    public List<EventDto> getAll(@Valid final AdminParameterDto adminParameterDto) {
        log.info("Administrator's request to provide {} by parameters - {}", SIMPLE_NAME, adminParameterDto);
        return eventService.getAllByAdmin(adminParameterDto);
    }

    @PatchMapping("/{eventId}")
    public EventDto update(@RequestBody @Valid final UpdateEventDto updateEventDto,
                           @PathVariable @Positive final long eventId) {
        log.info("Request by the administrator to change an {} by id - {} - {}", SIMPLE_NAME, eventId, updateEventDto);
        return eventService.updateByAdmin(eventId, updateEventDto);
    }

    @Override
    @GetMapping("/ids")
    public List<EventDto> getAllByIds(@RequestParam Set<Long> ids) {
        log.info("Administrator's request to provide {} by ids - {}", SIMPLE_NAME, ids);
        return eventService.getAllByIds(ids);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(EventDto event) {
        log.info("Administrator's request to save {} - {}", SIMPLE_NAME, event);
        eventService.save(event);
    }

    @Override
    @GetMapping("/existsByCategoryId")
    public boolean existsByCategoryId(@RequestParam(name = "existsByCategoryId") long categoryId) {
        log.info("Administrator's request to provide {} by id - {}", SIMPLE_NAME, categoryId);
        return eventService.existsByCategoryId(categoryId);
    }
}
