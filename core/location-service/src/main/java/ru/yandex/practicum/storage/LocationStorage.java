package ru.yandex.practicum.storage;

import ru.yandex.practicum.location.model.Location;
import ru.yandex.practicum.location.model.dto.LocationDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LocationStorage {
    Location save(Location location);

    Optional<Location> findByLatAndLon(final double lat, final double lon);

    Optional<Location> findById(final long id);

    Location findByIdOrElseThrow(final long id);

    void existsByIdOrElseThrow(final long id);

    void deleteById(final long id);

    List<Location> findAllByNameContainingIgnoreCase(final String text, final int from, final int size);

    List<Location> getAllByIds(Set<Long> locIds);
}
