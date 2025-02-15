package ru.yandex.practicum.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.user.model.dto.CreateUserDto;
import ru.yandex.practicum.user.model.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface UserService {
    UserDto create(CreateUserDto createUserDto);

    List<UserDto> getAll(final List<Long> ids, final int from, final int size);

    UserDto getById(final long id);

    void deleteById(final long id);

    List<UserDto> getAllByIds(Set<Long> ids);
}
