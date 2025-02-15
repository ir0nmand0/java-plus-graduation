package ru.yandex.practicum.user.converter;

import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.model.dto.CreateUserDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreateUserDtoToUserConverter implements Converter<CreateUserDto, User> {
    @Override
    public User convert(final CreateUserDto src) {
        return User.builder()
                .email(src.email())
                .name(src.name())
                .build();
    }
}
