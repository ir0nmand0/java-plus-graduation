package ru.yandex.practicum.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.location.model.dto.CreateLocationDto;
import ru.yandex.practicum.location.model.dto.LocationDto;
import ru.yandex.practicum.location.model.dto.LocationLatAndLonDto;
import ru.yandex.practicum.location.model.dto.UpdateLocationDto;

import java.util.List;
import java.util.Set;


public interface LocationService {

    LocationDto create(CreateLocationDto createLocationDto);

    LocationDto getByCoordinatesOrElseCreate(final LocationLatAndLonDto locationLatAndLonDto);

    LocationDto getById(final long id);

    LocationDto updateById(final long id, final UpdateLocationDto updateLocationDto);

    void deleteById(final long id);

    List<LocationDto> getAll(final String text, final int from, final int size);

    List<LocationDto> getAllByIds(Set<Long> locIds);
}
