package ru.yandex.practicum.user.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.user.model.dto.CreateUserDto;
import ru.yandex.practicum.user.model.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface AdminUserApi {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto create(@Valid @RequestBody final CreateUserDto createUserDto);

    @GetMapping
    List<UserDto> getAll(@RequestParam(required = false) final List<Long> ids,
                                @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
                                @RequestParam(defaultValue = "10") @Positive final int size);

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable @Positive final long userId);

    @GetMapping("/{userId}")
    UserDto getById(@PathVariable @Positive long userId);

    @GetMapping("/ids")
    List<UserDto> getAllByIds(@RequestParam Set<@Positive Long> ids);
}
