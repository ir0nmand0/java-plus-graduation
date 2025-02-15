package ru.yandex.practicum.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.dto.RequestDto;
import ru.yandex.practicum.state.State;

import java.util.List;
import java.util.Set;

public interface RequestService {
    RequestDto create(final long userId, final long eventId);

    List<RequestDto> getAll(final long userId);

    RequestDto cancel(final long userId, final long requestId);

    List<RequestDto> findAllByEventId(long eventId);

    List<Request> findAllByIdInAndEventId(Set<Long> requestIds, long eventId);

    int countByEventIdAndStatus(long eventId, State state);

    void saveAll(List<Request> requests);
}
