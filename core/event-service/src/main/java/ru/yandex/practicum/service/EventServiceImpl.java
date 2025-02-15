package ru.yandex.practicum.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.yandex.practicum.SearchStats;
import ru.yandex.practicum.category.client.PublicCategoryClient;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.model.dto.CategoryDto;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dto.CreateStatsDto;
import ru.yandex.practicum.dto.StatCountHitsDto;
import ru.yandex.practicum.event.model.AdminParameter;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.PublicParameter;
import ru.yandex.practicum.event.model.dto.CreateEventDto;
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.event.model.dto.UpdateEventDto;
import ru.yandex.practicum.exception.type.ConflictException;
import ru.yandex.practicum.exception.type.NotFoundException;
import ru.yandex.practicum.location.client.AdminLocationClient;
import ru.yandex.practicum.location.model.Location;
import ru.yandex.practicum.location.model.dto.LocationDto;
import ru.yandex.practicum.request.client.PrivateUserRequestClient;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.dto.RequestDto;
import ru.yandex.practicum.request.model.dto.RequestStatusUpdateResultDto;
import ru.yandex.practicum.request.model.dto.UpdateRequestByIdsDto;
import ru.yandex.practicum.state.State;
import ru.yandex.practicum.storage.EventStorage;
import ru.yandex.practicum.user.client.AdminUserClient;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.model.dto.UserDto;
import ru.yandex.practicum.user.model.dto.UserWithoutEmailDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.yandex.practicum.event.model.QEvent.event;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final String SIMPLE_NAME = Event.class.getSimpleName();
    @Qualifier("mvcConversionService")
    private final ConversionService cs;
    private final EventStorage eventStorage;
    private final AdminUserClient adminUserClient;
    private final PublicCategoryClient publicCategoryClient;
    private final AdminLocationClient adminLocationClient;
    private final PrivateUserRequestClient privateUserRequestClient;
    private final StatsClient statsClient;

    @Override
    public List<EventDto> getAllByAdmin(final AdminParameter adminParameter) {
        final List<Event> lists = eventStorage.findAll(getSpecification(adminParameter),
                PageRequest.of(adminParameter.getFrom() / adminParameter.getSize(),
                        adminParameter.getSize()));

        lists.forEach(event -> updateStats(event, adminParameter.getRangeStart(), adminParameter.getRangeEnd(), true));

        Set<Long> categoryIds = lists.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());

        Set<Long> usersIds = lists.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Set<Long> locIds = lists.stream()
                .map(Event::getLocationId)
                .collect(Collectors.toSet());

        List<CategoryDto> categoryDtos = publicCategoryClient.getAllByIds(categoryIds);

        List<UserDto> userDtos = adminUserClient.getAllByIds(usersIds);

        List<LocationDto> locationDtos = adminLocationClient.getAllByIds(locIds);

        List<UserWithoutEmailDto> withoutEmailDtos = userDtos.stream()
                .map(userDto -> cs.convert(userDto, UserWithoutEmailDto.class))
                .toList();

        Map<Long, UserWithoutEmailDto> userWithoutEmailDtoMap = withoutEmailDtos.stream()
                .collect(Collectors.toMap(UserWithoutEmailDto::id, Function.identity()));

        Map<Long, CategoryDto> categoryMap = categoryDtos.stream()
                .collect(Collectors.toMap(CategoryDto::id, Function.identity()));

        Map<Long, LocationDto> localMap = locationDtos.stream()
                .collect(Collectors.toMap(LocationDto::id, Function.identity()));

        return lists.stream()
                .map(event -> {
                            EventDto converted = cs.convert(event, EventDto.class);
                            converted.setCategory(categoryMap.get(event.getCategoryId()));
                            converted.setInitiator(userWithoutEmailDtoMap.get(event.getInitiatorId()));
                            converted.setLocation(localMap.get(event.getLocationId()));
                            return converted;
                        }
                )
                .toList();
    }

    @Override
    public List<EventDto> getAllByIds(Set<Long> ids) {
        if (ObjectUtils.isEmpty(ids)) {
            return List.of();
        }

        List<Event> events = eventStorage.findAllById(ids);

        // Собираем id для связанных сущностей
        Set<Long> categoryIds = events.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());
        Set<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Set<Long> locationIds = events.stream()
                .map(Event::getLocationId)
                .collect(Collectors.toSet());

        // Получаем связанные сущности
        Map<Long, CategoryDto> categoryMap = publicCategoryClient.getAllByIds(categoryIds).stream()
                .collect(Collectors.toMap(CategoryDto::id, c -> c));
        Map<Long, UserWithoutEmailDto> userMap = adminUserClient.getAllByIds(initiatorIds).stream()
                .map(user -> cs.convert(user, UserWithoutEmailDto.class))
                .collect(Collectors.toMap(UserWithoutEmailDto::id, u -> u));
        Map<Long, LocationDto> locationMap = adminLocationClient.getAllByIds(locationIds).stream()
                .collect(Collectors.toMap(LocationDto::id, l -> l));

        // Конвертируем и заполняем
        return events.stream()
                .map(event -> {
                    EventDto dto = cs.convert(event, EventDto.class);
                    dto.setCategory(categoryMap.get(event.getCategoryId()));
                    dto.setInitiator(userMap.get(event.getInitiatorId()));
                    dto.setLocation(locationMap.get(event.getLocationId()));
                    return dto;
                })
                .toList();
    }


    private Event update(Event eventInStorage, final UpdateEventDto updateEventDto) {

        if (!ObjectUtils.isEmpty(updateEventDto.participantLimit())) {
            eventInStorage.setParticipantLimit(updateEventDto.participantLimit());
        }

        if (!ObjectUtils.isEmpty(updateEventDto.paid())) {
            eventInStorage.setPaid(updateEventDto.paid());
        }

        if (!ObjectUtils.isEmpty(updateEventDto.annotation())) {
            eventInStorage.setAnnotation(updateEventDto.annotation());
        }

        if (!ObjectUtils.isEmpty(updateEventDto.description())) {
            eventInStorage.setDescription(updateEventDto.description());
        }

        if (!ObjectUtils.isEmpty(updateEventDto.title())) {
            eventInStorage.setTitle(updateEventDto.title());
        }

        if (updateEventDto.category() != 0) {
            CategoryDto byId = publicCategoryClient.getById(updateEventDto.category());

            if (ObjectUtils.isEmpty(byId)) {
                throw new NotFoundException(SIMPLE_NAME, updateEventDto.category());
            }

            eventInStorage.setCategoryId(updateEventDto.category());
        }

        return eventInStorage;
    }

    @Override
    public EventDto updateByAdmin(final long eventId, final UpdateEventDto updateEventDto) {
        Event eventInStorage = eventStorage.getByIdOrElseThrow(eventId);

        if (!ObjectUtils.isEmpty(updateEventDto.stateAction())) {
            switch (updateEventDto.stateAction()) {
                case REJECT_EVENT -> {
                    checkEventIsPublished(eventInStorage.getState());
                    eventInStorage.setState(State.CANCELED);
                }

                case PUBLISH_EVENT -> {
                    if (eventInStorage.getState() != State.PENDING) {
                        throw new ConflictException("An event can only be published if it is in a pending publication state");
                    }
                    if (LocalDateTime.now().plusHours(1).isAfter(eventInStorage.getEventDate())) {
                        throw new ConflictException("The start date of the event being modified must be no earlier "
                                + "than an hour before from date of publication");
                    }

                    eventInStorage.setState(State.PUBLISHED);
                    eventInStorage.setPublishedOn(LocalDateTime.now());
                }
            }
        }

        LocationDto locationDto = null;

        if (!ObjectUtils.isEmpty(updateEventDto.location())) {
            locationDto = adminLocationClient.getByCoordinatesOrElseCreate(updateEventDto.location());
            eventInStorage.setLocationId(locationDto.id());
        }

        Event inStorage = update(eventInStorage, updateEventDto);

        Event save = eventStorage.save(inStorage);

        CategoryDto categoryDto = publicCategoryClient.getById(save.getCategoryId());

        UserDto userDto = adminUserClient.getById(save.getInitiatorId());

        UserWithoutEmailDto withoutEmailDto = cs.convert(userDto, UserWithoutEmailDto.class);

        EventDto eventDto = cs.convert(save, EventDto.class);

        eventDto.setCategory(categoryDto);

        eventDto.setInitiator(withoutEmailDto);

        // Если в обновлении не передавалась локация, подгружаем существующую
        if (locationDto != null) {
            eventDto.setLocation(locationDto);
        } else {
            LocationDto currentLocation = adminLocationClient.getById(save.getLocationId());
            eventDto.setLocation(currentLocation);
        }

        return eventDto;
    }

    @Override
    public EventDto create(final CreateEventDto createEventDto, final long userId) {
        final UserDto userDto = adminUserClient.getById(userId);

        if (ObjectUtils.isEmpty(userDto)) {
            throw new NotFoundException(SIMPLE_NAME, userId);
        }

        final User user = cs.convert(userDto, User.class);

        final CategoryDto categoryDto = publicCategoryClient.getById(createEventDto.category());

        if (ObjectUtils.isEmpty(categoryDto)) {
            throw new NotFoundException(SIMPLE_NAME, createEventDto.category());
        }

        final Category category = cs.convert(categoryDto, Category.class);

        final Location location = cs.convert(adminLocationClient.getByCoordinatesOrElseCreate(createEventDto.location()),
                Location.class);

        Event event = cs.convert(createEventDto, Event.class);

        event.setInitiatorId(user.getId());
        event.setCategoryId(category.getId());
        event.setLocationId(location.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);

        Event save = eventStorage.save(event);

        EventDto eventDto = cs.convert(save, EventDto.class);

        UserWithoutEmailDto withoutEmailDto = cs.convert(userDto, UserWithoutEmailDto.class);

        eventDto.setCategory(categoryDto);
        eventDto.setInitiator(withoutEmailDto);
        eventDto.setLocation(cs.convert(location, LocationDto.class));

        return eventDto;
    }

    @Override
    public List<EventDto> getAllByUserId(final long userId, final int from, final int size) {
        final UserDto userDto = adminUserClient.getById(userId);

        if (ObjectUtils.isEmpty(userDto)) {
            throw new NotFoundException(SIMPLE_NAME, userId);
        }

        List<Event> lists = eventStorage.findAllByInitiatorId(userId, PageRequest.of(from, size));

        Set<Long> categoryIds = lists.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());

        Set<Long> usersIds = lists.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Set<Long> locIds = lists.stream()
                .map(Event::getLocationId)
                .collect(Collectors.toSet());

        List<CategoryDto> categoryDtos = publicCategoryClient.getAllByIds(categoryIds);

        List<UserDto> userDtos = adminUserClient.getAllByIds(usersIds);

        List<LocationDto> locationDtos = adminLocationClient.getAllByIds(locIds);

        List<UserWithoutEmailDto> withoutEmailDtos = userDtos.stream()
                .map(user -> cs.convert(user, UserWithoutEmailDto.class))
                .toList();

        Map<Long, UserWithoutEmailDto> userWithoutEmailDtoMap = withoutEmailDtos.stream()
                .collect(Collectors.toMap(UserWithoutEmailDto::id, Function.identity()));

        Map<Long, CategoryDto> categoryMap = categoryDtos.stream()
                .collect(Collectors.toMap(CategoryDto::id, Function.identity()));

        Map<Long, LocationDto> localMap = locationDtos.stream()
                .collect(Collectors.toMap(LocationDto::id, Function.identity()));

        return lists.stream()
                .map(event -> {
                            EventDto converted = cs.convert(event, EventDto.class);
                            converted.setCategory(categoryMap.get(event.getCategoryId()));
                            converted.setInitiator(userWithoutEmailDtoMap.get(event.getInitiatorId()));
                            converted.setLocation(localMap.get(event.getLocationId()));
                            return converted;
                        }
                )
                .toList();
    }

    @Override
    public List<RequestDto> getRequestsByUserIdAndEventId(final long userId, final long eventId) {
        final UserDto userDto = adminUserClient.getById(userId);

        if (ObjectUtils.isEmpty(userDto)) {
            throw new NotFoundException(SIMPLE_NAME, userId);
        }

        eventStorage.existsByIdOrElseThrow(eventId);

        return privateUserRequestClient.findAllByEventId(userId, eventId);
    }

    @Override
    public EventDto getByIdAndUserId(final long eventId, final long userId) {
        final UserDto userDto = adminUserClient.getById(userId);

        if (ObjectUtils.isEmpty(userDto)) {
            throw new NotFoundException(SIMPLE_NAME, userId);
        }

        Event event = eventStorage.getByIdOrElseThrow(eventId);
        checkIfTheUserIsTheEventCreator(userId, eventId);

        CategoryDto categoryDto = publicCategoryClient.getById(event.getCategoryId());

        LocationDto locationDto = adminLocationClient.getById(event.getLocationId());

        EventDto eventDto = cs.convert(event, EventDto.class);

        eventDto.setCategory(categoryDto);
        eventDto.setInitiator(cs.convert(userDto, UserWithoutEmailDto.class));
        eventDto.setLocation(locationDto);

        return eventDto;
    }

    @Override
    public RequestStatusUpdateResultDto updateRequestsStatusByUserIdAndEventId(final long userId, final long eventId,
                                                                               UpdateRequestByIdsDto update) {

        List<Request> requests = privateUserRequestClient.findAllByIdInAndEventId(userId, update.requestIds(), eventId);

        if (ObjectUtils.isEmpty(requests)) {
            throw new NotFoundException("No requests found for event id " + eventId);
        }

        int countRequest = privateUserRequestClient.countByEventIdAndStatus(userId, eventId, State.CONFIRMED);

        RequestStatusUpdateResultDto result = RequestStatusUpdateResultDto.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();

        List<Request> requestsForSave = new ArrayList<>();

        for (Request request : requests) {
            if (request.getStatus() != State.PENDING) {
                throw new ConflictException(
                        "The status can only be changed for applications that are in a pending state"
                );
            }

            Event event = eventStorage.getById(request.getEventId())
                    .orElseThrow(() -> new NotFoundException(SIMPLE_NAME, request.getEventId()));

            if (countRequest >= event.getParticipantLimit()) {
                throw new ConflictException("The limit on applications for this event has been reached");
            }

            if (event.getParticipantLimit() != 0 && event.isRequestModeration()) {
                request.setStatus(update.status());

                if (countRequest++ == event.getParticipantLimit()) {
                    request.setStatus(State.CANCELED);
                }

                requestsForSave.add(request);

                if (update.status() == State.CONFIRMED) {
                    result.confirmedRequests().add(cs.convert(request, RequestDto.class));
                }

                if (update.status() == State.REJECTED) {
                    result.rejectedRequests().add(cs.convert(request, RequestDto.class));
                }
            }
        }

        if (!requestsForSave.isEmpty()) {
            privateUserRequestClient.saveAll(userId, requestsForSave);
        }

        return result;
    }

    @Override
    public EventDto getById(final long eventId, final HttpServletRequest request) {
        Event event = eventStorage.getByIdOrElseThrow(eventId);

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException(SIMPLE_NAME, eventId);
        }

        addStats(request);

        updateStats(event, LocalDateTime.now().minusDays(3), LocalDateTime.now().plusDays(3), true);

        CategoryDto categoryDto = publicCategoryClient.getById(event.getCategoryId());

        UserDto userDto = adminUserClient.getById(event.getInitiatorId());

        UserWithoutEmailDto userWithoutEmailDto = cs.convert(userDto, UserWithoutEmailDto.class);

        Event save = eventStorage.save(event);

        EventDto eventDto = cs.convert(save, EventDto.class);

        eventDto.setCategory(categoryDto);
        eventDto.setInitiator(userWithoutEmailDto);
        eventDto.setLocation(adminLocationClient.getById(event.getLocationId()));

        return eventDto;
    }

    @Override
    public List<EventDto> getAll(final PublicParameter publicParameter, final HttpServletRequest request) {
        BooleanExpression predicate = event.isNotNull();

        if (!ObjectUtils.isEmpty(publicParameter.getText())) {
            predicate = predicate.and(event.annotation.likeIgnoreCase(publicParameter.getText()));
        }

        if (!ObjectUtils.isEmpty(publicParameter.getCategories())) {
            predicate = predicate.and(event.categoryId.in(publicParameter.getCategories()));
        }
        if (!ObjectUtils.isEmpty(publicParameter.getPaid())) {
            predicate = predicate.and(event.paid.eq(publicParameter.getPaid()));
        }

        predicate = predicate.and(event.createdOn.between(publicParameter.getRangeStart(), publicParameter.getRangeEnd()));

        addStats(request);

        final List<Event> lists = eventStorage.findAll(
                predicate, PageRequest.of(publicParameter.getFrom() / publicParameter.getSize(),
                        publicParameter.getSize())
        );

        lists.forEach(event -> updateStats(event, publicParameter.getRangeStart(), publicParameter.getRangeEnd(), false));

        eventStorage.saveAll(lists);

        Set<Long> categoryIds = lists.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());

        Set<Long> usersIds = lists.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Set<Long> locIds = lists.stream()
                .map(Event::getLocationId)
                .collect(Collectors.toSet());

        List<CategoryDto> categoryDtos = publicCategoryClient.getAllByIds(categoryIds);

        List<UserDto> userDtos = adminUserClient.getAllByIds(usersIds);

        List<LocationDto> locationDtos = adminLocationClient.getAllByIds(locIds);

        List<UserWithoutEmailDto> withoutEmailDtos = userDtos.stream()
                .map(user -> cs.convert(user, UserWithoutEmailDto.class))
                .toList();

        Map<Long, UserWithoutEmailDto> userWithoutEmailDtoMap = withoutEmailDtos.stream()
                .collect(Collectors.toMap(UserWithoutEmailDto::id, Function.identity()));

        Map<Long, CategoryDto> categoryMap = categoryDtos.stream()
                .collect(Collectors.toMap(CategoryDto::id, Function.identity()));

        Map<Long, LocationDto> localMap = locationDtos.stream()
                .collect(Collectors.toMap(LocationDto::id, Function.identity()));

        return lists.stream()
                .map(event -> {
                            EventDto converted = cs.convert(event, EventDto.class);
                            converted.setCategory(categoryMap.get(event.getCategoryId()));
                            converted.setInitiator(userWithoutEmailDtoMap.get(event.getInitiatorId()));
                            converted.setLocation(localMap.get(event.getLocationId()));
                            return converted;
                        }
                )
                .toList();
    }

    @Override
    public List<EventDto> getAllByLocation(final double lat, final double lon, final double radius) {
        List<Event> events;

        if (radius > 0) {
            events = eventStorage.findAllByLocationAndRadius(lat, lon, radius);
        } else {
            events = eventStorage.findAllEventsByLocation(lat, lon);
        }

        if (ObjectUtils.isEmpty(events)) {
            return List.of();
        }

        Set<Long> categoryIds = events.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());

        Set<Long> usersIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Set<Long> locIds = events.stream()
                .map(Event::getLocationId)
                .collect(Collectors.toSet());

        List<CategoryDto> categoryDtos = publicCategoryClient.getAllByIds(categoryIds);

        List<UserDto> userDtos = adminUserClient.getAllByIds(usersIds);

        List<LocationDto> locationDtos = adminLocationClient.getAllByIds(locIds);

        List<UserWithoutEmailDto> withoutEmailDtos = userDtos.stream()
                .map(user -> cs.convert(user, UserWithoutEmailDto.class))
                .toList();

        Map<Long, UserWithoutEmailDto> userWithoutEmailDtoMap = withoutEmailDtos.stream()
                .collect(Collectors.toMap(UserWithoutEmailDto::id, Function.identity()));

        Map<Long, CategoryDto> categoryMap = categoryDtos.stream()
                .collect(Collectors.toMap(CategoryDto::id, Function.identity()));

        Map<Long, LocationDto> localMap = locationDtos.stream()
                .collect(Collectors.toMap(LocationDto::id, Function.identity()));

        return events.stream()
                .map(event -> {
                            EventDto converted = cs.convert(event, EventDto.class);
                            converted.setCategory(categoryMap.get(event.getCategoryId()));
                            converted.setInitiator(userWithoutEmailDtoMap.get(event.getInitiatorId()));
                            converted.setLocation(localMap.get(event.getLocationId()));
                            return converted;
                        }
                )
                .toList();
    }

    @Override
    public EventDto updateByUser(final long userId, final long eventId, final UpdateEventDto updateEventDto) {
        Event eventInStorage = eventStorage.getByIdOrElseThrow(eventId);

        checkEventIsPublished(eventInStorage.getState());

        if (eventInStorage.getInitiatorId() != userId) {
            throw new ConflictException("The initiator does not belong to this event");
        }

        if (!ObjectUtils.isEmpty(updateEventDto.stateAction())) {
            switch (updateEventDto.stateAction()) {
                case CANCEL_REVIEW -> eventInStorage.setState(State.CANCELED);

                case SEND_TO_REVIEW -> eventInStorage.setState(State.PENDING);
            }
        }

        Event save = eventStorage.save(update(eventInStorage, updateEventDto));

        CategoryDto categoryDto = publicCategoryClient.getById(save.getCategoryId());

        UserDto userDto = adminUserClient.getById(save.getInitiatorId());

        UserWithoutEmailDto userWithoutEmailDto = cs.convert(userDto, UserWithoutEmailDto.class);

        EventDto eventDto = cs.convert(save, EventDto.class);

        eventDto.setCategory(categoryDto);
        eventDto.setInitiator(userWithoutEmailDto);
        eventDto.setLocation(adminLocationClient.getById(save.getLocationId()));

        return eventDto;
    }

    @Override
    public void save(EventDto event) {
        eventStorage.save(cs.convert(event, Event.class));
    }

    @Override
    public boolean existsByCategoryId(long categoryId) {
        return eventStorage.existsByCategoryId(categoryId);
    }

    private void addStats(final HttpServletRequest request) {
        statsClient.save(CreateStatsDto.builder()
                .app("ewm")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    private void updateStats(Event event, final LocalDateTime startRange, final LocalDateTime endRange,
                             final boolean unique) {

        List<StatCountHitsDto> stats = statsClient.get(SearchStats.builder()
                .start(startRange)
                .end(endRange)
                .uris(List.of("/events/" + event.getId()))
                .unique(unique)
                .build()
        );
        long views = 0L;

        for (StatCountHitsDto stat : stats) {
            views += stat.hits();
        }

        int confirmedRequests = privateUserRequestClient.countByEventIdAndStatus(event.getInitiatorId(),
                event.getId(), State.CONFIRMED);

        event.setViews(views);
        event.setConfirmedRequests(confirmedRequests);
    }

    private void checkIfTheUserIsTheEventCreator(final long userId, final long eventId) {
        if (userId == eventId) {
            throw new ConflictException(String.format("Event originator userId: %d cannot add a membership request " +
                    "in its event eventId: %d", userId, eventId));
        }
    }

    private Specification<Event> checkCategories(final List<Long> categories) {
        return ObjectUtils.isEmpty(categories) ? null
                : ((root, query, criteriaBuilder) -> root.get("categoryId").in(categories));
    }

    private Specification<Event> checkByUserIds(final List<Long> userIds) {
        return ObjectUtils.isEmpty(userIds) ? null
                : ((root, query, criteriaBuilder) -> root.get("initiatorId").in(userIds));
    }

    private Specification<Event> checkStates(final List<State> states) {
        return ObjectUtils.isEmpty(states) ? null
                : ((root, query, criteriaBuilder) -> root.get("state").as(String.class).in(states.stream()
                .map(Enum::toString)
                .toList())
        );
    }

    private Specification<Event> checkRangeStart(final LocalDateTime start) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"),
                        start == null ? LocalDateTime.now() : start));
    }

    private Specification<Event> checkRangeEnd(final LocalDateTime end) {
        return ObjectUtils.isEmpty(end) ? null
                : ((root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("eventDate"), end));
    }

    private Specification<Event> getSpecification(final AdminParameter adminParameter) {
        return Specification.where(checkByUserIds(adminParameter.getUsers()))
                .and(checkStates(adminParameter.getStates()))
                .and(checkCategories(adminParameter.getCategories()))
                .and(checkRangeStart(adminParameter.getRangeStart()))
                .and(checkRangeEnd(adminParameter.getRangeEnd()));
    }

    private BooleanExpression getPredicate(final AdminParameter adminParameter) {
        BooleanExpression predicate = event.isNotNull();

        if (!ObjectUtils.isEmpty(adminParameter.getUsers())) {
            predicate = predicate.and(event.initiatorId.in(adminParameter.getUsers()));
        }

        if (!ObjectUtils.isEmpty(adminParameter.getStates())) {
            predicate = predicate.and(event.state.in(adminParameter.getStates()));
        }

        if (!ObjectUtils.isEmpty(adminParameter.getCategories())) {
            predicate = predicate.and(event.categoryId.in(adminParameter.getCategories()));
        }

        predicate = predicate.and(event.createdOn.between(adminParameter.getRangeStart(), adminParameter.getRangeEnd()));

        return predicate;
    }

    private void checkEventIsPublished(final State state) {
        if (state == State.PUBLISHED) {
            throw new ConflictException("An event can only be rejected if it has not yet been published");
        }
    }
}
