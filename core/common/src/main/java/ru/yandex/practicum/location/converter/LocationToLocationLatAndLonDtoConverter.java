package ru.yandex.practicum.location.converter;

import ru.yandex.practicum.location.model.Location;
import ru.yandex.practicum.location.model.dto.LocationLatAndLonDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LocationToLocationLatAndLonDtoConverter implements Converter<Location, LocationLatAndLonDto> {
    @Override
    public LocationLatAndLonDto convert(final Location source) {
        return LocationLatAndLonDto.builder()
                .lon(source.getLon())
                .lat(source.getLat())
                .build();
    }
}
