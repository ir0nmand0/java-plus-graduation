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
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.exception.type.ConflictException;
import ru.yandex.practicum.exception.type.NotFoundException;
import ru.yandex.practicum.entity.Request;
import ru.yandex.practicum.request.model.dto.RequestDto;
import ru.yandex.practicum.state.State;
import ru.yandex.practicum.storage.RequestStorage;
import ru.yandex.practicum.user.client.AdminUserClient;
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

        // Получаем EventDto напрямую
        EventDto eventDto = adminEventClient.getAllByIds(Set.of(eventId)).getFirst();
        if (ObjectUtils.isEmpty(eventDto)) {
            throw new NotFoundException(SIMPLE_NAME, eventId);
        }

        log.info("Create request for user {} with id {} and event {} and initiator {}",
                userDto, userDto.id(), eventDto.getId(), eventDto.getInitiator().id());

        // Проверяем, что пользователь не является инициатором события
        if (eventDto.getInitiator().id() == userDto.id()) {
            throw new ConflictException("%s : can`t add a request to your own: %d eventId: %d"
                    .formatted(SIMPLE_NAME, userId, eventId));
        }

        if (eventDto.getState() != State.PUBLISHED) {
            throw new ConflictException("Cannot add a request to an unpublished eventId: %d".formatted(eventId));
        } else if (eventDto.getParticipantLimit() != 0
                && requestStorage.countByEventIdAndStatus(eventDto.getId(), State.CONFIRMED) >= eventDto.getParticipantLimit()) {
            throw new ConflictException("Event participation limit exceeded eventId: %d".formatted(eventId));
        }

        // Создаем запрос, используя данные из DTO
        Request request = Request.builder()
                .created(LocalDateTime.now())
                .eventId(eventDto.getId())
                .requesterId(userDto.id())
                .status(eventDto.getParticipantLimit() == 0 || !eventDto.isRequestModeration() ? State.CONFIRMED : State.PENDING)
                .build();

        // Если запрос подтвержден, обновляем данные события через DTO
        if (request.getStatus() == State.CONFIRMED) {
            eventDto.setConfirmedRequests(eventDto.getConfirmedRequests() + 1);

            CategoryDto categoryDto = publicCategoryClient.getById(eventDto.getCategory().id());
            eventDto.setCategory(categoryDto);
            UserWithoutEmailDto initiatorDto = cs.convert(userDto, UserWithoutEmailDto.class);
            eventDto.setInitiator(initiatorDto);

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
            // Получаем EventDto напрямую
            EventDto eventDto = adminEventClient.getAllByIds(Set.of(request.getEventId())).getFirst();

            // Обновляем количество подтвержденных запросов
            eventDto.setConfirmedRequests(eventDto.getConfirmedRequests() - 1);

            // Обновляем инициатора через DTO
            eventDto.setInitiator(cs.convert(userDto, UserWithoutEmailDto.class));

            // Получаем и устанавливаем категорию
            CategoryDto categoryDto = publicCategoryClient.getById(eventDto.getCategory().id());
            eventDto.setCategory(categoryDto);

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
    public List<RequestDto> findAllByIdInAndEventId(Set<Long> requestIds, long eventId) {
        return requestStorage.findAllByIdInAndEventId(requestIds, eventId).stream()
                .map(request -> cs.convert(request, RequestDto.class))
                .toList();
    }

    @Override
    public int countByEventIdAndStatus(long eventId, State state) {
        return requestStorage.countByEventIdAndStatus(eventId, state);
    }

    @Override
    public void saveAll(List<RequestDto> requests) {
        requestStorage.saveAll(requests.stream().map(requestDto -> cs.convert(requestDto, Request.class)).toList());
    }
}
