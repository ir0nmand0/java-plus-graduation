package ru.yandex.practicum.location.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.location.model.dto.LocationDto;

import java.util.List;

@RequestMapping("/locations")
public interface PublicLocationApi {
    @GetMapping
    List<LocationDto> getAll(@RequestParam(required = false, defaultValue = "") final String text,
                             @RequestParam(defaultValue = "0") final int from,
                             @RequestParam(defaultValue = "10") final int size);
}
