package ru.yandex.practicum.user.converter;

import ru.yandex.practicum.user.model.dto.UserDto;
import ru.yandex.practicum.user.model.dto.UserWithoutEmailDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserDtoToUserWithoutEmailDtoConverter implements Converter<UserDto, UserWithoutEmailDto> {
    @Override
    public UserWithoutEmailDto convert(final UserDto src) {
        return UserWithoutEmailDto.builder()
                .id(src.id())
                .name(src.name())
                .build();
    }
}