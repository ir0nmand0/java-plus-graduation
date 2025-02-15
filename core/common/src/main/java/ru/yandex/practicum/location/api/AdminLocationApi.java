package ru.yandex.practicum.location.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.location.model.dto.CreateLocationDto;
import ru.yandex.practicum.location.model.dto.LocationDto;
import ru.yandex.practicum.location.model.dto.LocationLatAndLonDto;
import ru.yandex.practicum.location.model.dto.UpdateLocationDto;

import java.util.List;
import java.util.Set;

public interface AdminLocationApi {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LocationDto create(@RequestBody @Valid final CreateLocationDto createLocationDto);

    @GetMapping("/{locId}")
    LocationDto getById(@PathVariable @Positive final long locId);

    @PatchMapping("/{locId}")
    LocationDto update(@RequestBody final UpdateLocationDto updateLocationDto,
                       @PathVariable @Positive final long locId);

    @DeleteMapping("/{locId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteById(@PathVariable final long locId);

    @PostMapping("/coordinates")
    LocationDto getByCoordinatesOrElseCreate(@RequestBody @Valid LocationLatAndLonDto locationLatAndLonDto);

    @GetMapping("/ids")
    List<LocationDto> getAllByIds(@RequestParam Set<@Positive Long> ids);
}
