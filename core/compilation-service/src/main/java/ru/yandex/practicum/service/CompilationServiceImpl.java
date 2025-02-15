package ru.yandex.practicum.service;


import ru.yandex.practicum.event.client.AdminEventClient;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.compilation.model.dto.CompilationDto;
import ru.yandex.practicum.compilation.model.dto.CreateCompilationDto;
import ru.yandex.practicum.compilation.model.dto.UpdateCompilationDto;
import ru.yandex.practicum.event.model.dto.EventDto;
import ru.yandex.practicum.storage.CompilationStorage;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.exception.type.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    @Qualifier("mvcConversionService")
    private final ConversionService cs;
    private final CompilationStorage compilationStorage;
    private final AdminEventClient adminEventClient;

    @Override
    public CompilationDto create(final CreateCompilationDto createCompilationDto) {
        Compilation compilation = cs.convert(createCompilationDto, Compilation.class);

        List<Event> eventList = new ArrayList<>();

        List<EventDto> events = null;

        if (!ObjectUtils.isEmpty(createCompilationDto.events())) {

            events = adminEventClient.getAllByIds(createCompilationDto.events());

            if (!ObjectUtils.isEmpty(events) && events.size() != createCompilationDto.events().size()) {
                throw new NotFoundException("the number of events found does not correspond to the requirements");
            }

            events.forEach(eventDto -> eventList.add(cs.convert(eventDto, Event.class)));

            List<Long> ids = eventList.stream()
                    .map(Event::getId)
                    .toList();

            compilation.setEventIds(ids);
        }

        CompilationDto compilationDto = cs.convert(compilationStorage.save(compilation), CompilationDto.class);

        compilationDto.setEvents(ObjectUtils.isEmpty(events) ? List.of() : events);

        return compilationDto;
    }

    @Override
    public CompilationDto update(final UpdateCompilationDto updateCompilationDto, final long compId) {
        Compilation compilationInStorage = compilationStorage.getByIdOrElseThrow(compId);

        if (ObjectUtils.isEmpty(updateCompilationDto.pinned())) {
            compilationInStorage.setPinned(compilationInStorage.isPinned());
        }

        if (ObjectUtils.isEmpty(updateCompilationDto.title())) {
            compilationInStorage.setTitle(compilationInStorage.getTitle());
        }

        List<Event> eventList = new ArrayList<>();

        List<EventDto> events = null;

        if (!ObjectUtils.isEmpty(updateCompilationDto.events())) {
            events = adminEventClient.getAllByIds(updateCompilationDto.events());

            events.forEach(eventDto -> eventList.add(cs.convert(eventDto, Event.class)));

            List<Long> ids = eventList.stream()
                    .map(Event::getId)
                    .toList();

            // Создаем новый ArrayList перед сохранением, чтобы Hibernate смог обновить коллекцию
            // (очистить старые значения и добавить новые)
            compilationInStorage.setEventIds(new ArrayList<>(ids));
        }

        log.info("Update compilation - {}", compilationInStorage);

        CompilationDto compilationDto = cs.convert(compilationStorage.save(compilationInStorage), CompilationDto.class);

        compilationDto.setEvents(ObjectUtils.isEmpty(events) ? List.of() : events);

        return compilationDto;
    }

    @Override
    public void delete(final long compId) {
        compilationStorage.existsByIdOrElseThrow(compId);
        compilationStorage.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getAll(final Boolean pinned, final int from, final int size) {
        PageRequest page = PageRequest.of(from / size, size);

        List<Compilation> compilations = ObjectUtils.isEmpty(pinned) ? compilationStorage.findAll(page)
                : compilationStorage.findAllByPinnedIs(pinned, page);

        Set<Long> allEventIds = compilations.stream()
                .map(Compilation::getEventIds)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        List<EventDto> eventsDto = adminEventClient.getAllByIds(allEventIds);

        Map<Long, EventDto> eventDtoMap = eventsDto.stream()
                .collect(Collectors.toMap(
                        EventDto::getId,   // ключ - id события
                        event -> event     // значение - само событие
                ));

        Map<Long, List<EventDto>> compilationEvents = compilations.stream()
                .collect(Collectors.toMap(
                        Compilation::getId,    // ключ - id подборки
                        compilation -> compilation.getEventIds().stream()  // для каждого eventId в подборке
                                .map(eventDtoMap::get)                        // получаем соответствующий EventDto
                                .filter(Objects::nonNull)                     // убираем null если вдруг какое-то событие не нашлось
                                .collect(Collectors.toList())                 // собираем в список
                ));

        return compilations.stream()
                .map(compilation -> {
                    CompilationDto compilationDto = cs.convert(compilation, CompilationDto.class);
                    compilationDto.setEvents(compilationEvents.get(compilationDto.getId()));
                    return compilationDto;
                })
                .toList();
    }

    @Override
    public CompilationDto getById(final long compId) {
        Compilation compilation = compilationStorage.getByIdOrElseThrow(compId);

        CompilationDto compilationDto = cs.convert(compilation, CompilationDto.class);

        Set<Long> ids = Set.copyOf(compilation.getEventIds());

        List<EventDto> eventsDto = adminEventClient.getAllByIds(ids);

        compilationDto.setEvents(eventsDto);

        return compilationDto;
    }
}
