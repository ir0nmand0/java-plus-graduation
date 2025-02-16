package ru.yandex.practicum.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.User;
import ru.yandex.practicum.user.model.dto.UserDto;

@Component
public class UserToUserDtoConverter implements Converter<User, UserDto> {
    @Override
    public UserDto convert(final User src) {
        return UserDto.builder()
                .id(src.getId())
                .email(src.getEmail())
                .name(src.getName())
                .build();
    }
}
