package ru.yandex.practicum.event.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.model.dto.AdminParameterDto;
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.event.model.dto.UpdateEventDto;

import java.util.List;
import java.util.Set;

public interface AdminEventApi {
    @GetMapping
    List<EventDto> getAll(@Valid final AdminParameterDto adminParameterDto);

    @PatchMapping("/{eventId}")
    EventDto update(@RequestBody @Valid final UpdateEventDto updateEventDto, @PathVariable @Positive final long eventId);

    @GetMapping("/ids")
    List<EventDto> getAllByIds(@RequestParam final Set<Long> ids);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void save(@RequestBody @Valid EventDto event);

    @GetMapping("/existsByCategoryId")
    boolean existsByCategoryId(@RequestParam(name = "existsByCategoryId") long categoryId);
}
