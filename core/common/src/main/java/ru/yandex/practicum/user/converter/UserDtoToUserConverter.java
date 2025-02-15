package ru.yandex.practicum.user.converter;

import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.model.dto.UserDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserDtoToUserConverter implements Converter<UserDto, User> {
    @Override
    public User convert(final UserDto src) {
        return User.builder()
                .id(src.id())
                .email(src.email())
                .name(src.name())
                .build();
    }
}