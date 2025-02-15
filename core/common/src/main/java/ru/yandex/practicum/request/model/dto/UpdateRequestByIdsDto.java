package ru.yandex.practicum.request.model.dto;

import ru.yandex.practicum.state.State;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record UpdateRequestByIdsDto(
        @NotEmpty
        Set<Long> requestIds,
        @NotNull
        State status
) {
}
