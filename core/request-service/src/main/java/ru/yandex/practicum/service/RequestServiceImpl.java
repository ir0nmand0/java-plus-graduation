package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.yandex.practicum.category.client.PublicCategoryClient;
import ru.yandex.practicum.category.model.dto.CategoryDto;
import ru.yandex.practicum.event.client.AdminEventClient;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.exception.type.ConflictException;
import ru.yandex.practicum.exception.type.NotFoundException;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.dto.RequestDto;
import ru.yandex.practicum.state.State;
import ru.yandex.practicum.storage.RequestStorage;
import ru.yandex.practicum.user.client.AdminUserClient;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.model.dto.UserDto;
import ru.yandex.practicum.user.model.dto.UserWithoutEmailDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private static final String SIMPLE_NAME = Request.class.getSimpleName();
    @Qualifier("mvcConversionService")
    private final ConversionService cs;
    private final RequestStorage requestStorage;
    private final AdminUserClient adminUserClient;
    private final AdminEventClient adminEventClient;
    private final PublicCategoryClient publicCategoryClient;

    @Override
    public RequestDto create(final long userId, final long eventId) {
        requestStorage.ifExistsByRequesterIdAndEventIdThenThrow(userId, eventId);
        final UserDto userDto = adminUserClient.getById(userId);

        if (ObjectUtils.isEmpty(userDto)) {
            throw new NotFoundException(SIMPLE_NAME, userId);
        }

        User user = cs.convert(userDto, User.class);

        EventDto first = adminEventClient.getAllByIds(Set.of(eventId)).getFirst();

        log.info("Create request for user {} with id {} and event {} and initiator {}",
                userDto, user.getId(), first.getId(), first.getInitiator().id());

        Event event = cs.convert(first, Event.class);

        event.setInitiatorId(first.getInitiator().id());

        if (ObjectUtils.isEmpty(event)) {
            throw new NotFoundException(SIMPLE_NAME, userId);
        }


        if (event.getInitiatorId() == user.getId()) {
            throw new ConflictException("%s : can`t add a request to your own: %d eventId: %d".formatted(SIMPLE_NAME,
                    userId, eventId));
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Cannot add a request to an unpublished eventId: %d".formatted(eventId));
        } else if (event.getParticipantLimit() != 0
                && requestStorage.countByEventIdAndStatus(event.getId(), State.CONFIRMED) >= event.getParticipantLimit()) {
            throw new ConflictException("Event participation limit exceeded eventId: %d".formatted(eventId));
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .eventId(event.getId())
                .requesterId(user.getId())
                .status(event.getParticipantLimit() == 0 || !event.isRequestModeration() ? State.CONFIRMED
                        : State.PENDING)
                .build();

        if (request.getStatus() == State.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);

            CategoryDto categoryDto = publicCategoryClient.getById(event.getCategoryId());

            EventDto eventDto = cs.convert(event, EventDto.class);

            eventDto.setCategory(categoryDto);
            eventDto.setInitiator(cs.convert(user, UserWithoutEmailDto.class));

            adminEventClient.save(eventDto);
        }

        return cs.convert(requestStorage.save(request), RequestDto.class);
    }

    @Override
    public List<RequestDto> getAll(final long userId) {
        return requestStorage.findAllByRequesterId(userId).stream()
                .map(request -> cs.convert(request, RequestDto.class))
                .toList();
    }

    @Override
    public RequestDto cancel(final long userId, final long requestId) {
        final UserDto userDto = adminUserClient.getById(userId);

        if (ObjectUtils.isEmpty(userDto)) {
            throw new NotFoundException(SIMPLE_NAME, userId);
        }

        Request request = requestStorage.getByIdOrElseThrow(requestId);

        if (request.getStatus() == State.CONFIRMED) {
            Event event = cs.convert(adminEventClient.getAllByIds(Set.of(request.getEventId())).getFirst(), Event.class);
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            event.setInitiatorId(userId);

            CategoryDto categoryDto = publicCategoryClient.getById(event.getCategoryId());

            EventDto eventDto = cs.convert(event, EventDto.class);

            eventDto.setCategory(categoryDto);
            eventDto.setInitiator(cs.convert(userDto, UserWithoutEmailDto.class));

            adminEventClient.save(eventDto);
        }

        request.setStatus(State.CANCELED);

        return cs.convert(requestStorage.save(request), RequestDto.class);
    }

    @Override
    public List<RequestDto> findAllByEventId(long eventId) {
        return requestStorage.findAllByEventId(eventId).stream()
                .map(request -> cs.convert(request, RequestDto.class))
                .toList();
    }

    @Override
    public List<Request> findAllByIdInAndEventId(Set<Long> requestIds, long eventId) {
        return requestStorage.findAllByIdInAndEventId(requestIds, eventId);
    }

    @Override
    public int countByEventIdAndStatus(long eventId, State state) {
        return requestStorage.countByEventIdAndStatus(eventId, state);
    }

    @Override
    public void saveAll(List<Request> requests) {
        requestStorage.saveAll(requests);
    }
}
