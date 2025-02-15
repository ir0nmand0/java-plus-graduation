package ru.yandex.practicum.event.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.event.model.PublicParameter;
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.location.validation.ConstraintNotZero;

import java.util.List;

@RequestMapping("/events")
public interface PublicEventApi {
    @GetMapping("/{id}")
    EventDto getById(@PathVariable @Positive final long id, final HttpServletRequest request);

    @GetMapping
    List<EventDto> getAll(@Valid final PublicParameter parameter,
                          final HttpServletRequest request);

    @GetMapping("/locations")
    List<EventDto> getEventsByLatAndLon(@RequestParam @ConstraintNotZero final Double lat,
                                        @RequestParam @ConstraintNotZero final Double lon,
                                        @RequestParam(required = false, defaultValue = "0")
                                        @PositiveOrZero final double radius);
}
