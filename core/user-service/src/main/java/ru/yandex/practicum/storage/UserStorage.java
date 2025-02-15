package ru.yandex.practicum.storage;

import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.model.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    User save(User user);

    void deleteById(final long id);

    List<User> getAll(final List<Long> ids, final int from, final int size);

    Optional<User> getById(final long id);

    User getByIdOrElseThrow(final long id);

    void existsByIdOrElseThrow(final long id);

    List<User> getAll(Set<Long> ids);
}
