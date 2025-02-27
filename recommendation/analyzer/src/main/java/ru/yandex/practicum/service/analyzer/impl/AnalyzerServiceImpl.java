package ru.yandex.practicum.service.analyzer.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.mapper.EventSimilarityMapper;
import ru.yandex.practicum.mapper.UserActionMapper;
import ru.yandex.practicum.model.EventSimilarity;
import ru.yandex.practicum.model.UserAction;
import ru.yandex.practicum.repository.EventSimilarityRepository;
import ru.yandex.practicum.repository.UserActionRepository;
import ru.yandex.practicum.service.analyzer.AnalyzerService;

import java.time.Instant;
import java.util.Optional;

/**
 * Реализация сервиса анализа данных.
 * Класс отвечает за сохранение и обновление данных о действиях пользователей
 * и о сходстве между событиями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalyzerServiceImpl implements AnalyzerService {
    /**
     * Репозиторий для работы с данными о сходстве событий
     */
    private final EventSimilarityRepository eventSimilarityRepository;

    /**
     * Репозиторий для работы с данными о действиях пользователей
     */
    private final UserActionRepository userActionRepository;

    /**
     * Маппер для преобразования Avro-объектов сходства событий в сущности БД
     */
    private final EventSimilarityMapper eventSimilarityMapper;

    /**
     * Маппер для преобразования Avro-объектов действий пользователей в сущности БД
     */
    private final UserActionMapper userActionMapper;

    /**
     * Сохраняет информацию о действии пользователя.
     *
     * @param userActionAvro Объект, содержащий данные о действии пользователя в формате Avro
     */
    @Override
    public void saveUserAction(UserActionAvro userActionAvro) {
        log.info("Начало сохранения действия пользователя: {}", userActionAvro);

        if (userActionAvro == null) {
            log.error("Получен null при попытке сохранить действие пользователя");
            throw new IllegalArgumentException("UserActionAvro не может быть null");
        }

        try {
            UserAction userAction = userActionMapper.toEntity(userActionAvro);

            if (userAction == null) {
                log.error("Маппер вернул null при преобразовании userActionAvro: {}", userActionAvro);
                throw new IllegalStateException("Ошибка при маппинге UserActionAvro в UserAction");
            }

            UserAction savedUserAction = userActionRepository.save(userAction);
            log.info("Успешно сохранено действие пользователя: userId={}, eventId={}, actionType={}",
                    savedUserAction.getUserId(), savedUserAction.getEventId(), savedUserAction.getActionType());
            log.info("Полные данные сохраненного действия: {}", savedUserAction);
        } catch (Exception e) {
            log.error("Ошибка при сохранении действия пользователя: {}", userActionAvro, e);
            throw e;
        }
    }

    /**
     * Сохраняет или обновляет информацию о сходстве между событиями.
     * Если запись о сходстве этих событий уже существует, то обновляет значение сходства и временную метку.
     * В противном случае создает новую запись.
     *
     * @param eventSimilarityAvro Объект, содержащий данные о сходстве событий в формате Avro
     */
    @Override
    public void saveEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Начало сохранения сходства событий: {}", eventSimilarityAvro);

        if (eventSimilarityAvro == null) {
            log.error("Получен null при попытке сохранить сходство событий");
            throw new IllegalArgumentException("EventSimilarityAvro не может быть null");
        }

        try {
            long eventA = eventSimilarityAvro.getEventA();
            long eventB = eventSimilarityAvro.getEventB();

            Optional<EventSimilarity> eventSimilarityOpt = eventSimilarityRepository
                    .findByEventAAndEventB(eventA, eventB);

            if (eventSimilarityOpt.isPresent()) {
                log.info("Найдена существующая запись о сходстве событий {} и {}", eventA, eventB);
                EventSimilarity existingEventSimilarity = eventSimilarityOpt.get();

                // Сохраняем предыдущее значение для логирования
                float oldScore = existingEventSimilarity.getScore();

                // Обновляем значения
                existingEventSimilarity.setScore(eventSimilarityAvro.getScore());
                existingEventSimilarity.setTimestamp(Instant.ofEpochSecond(eventSimilarityAvro.getTimestamp()));

                EventSimilarity updatedEventSimilarity = eventSimilarityRepository.save(existingEventSimilarity);
                log.info("Обновлено сходство событий: eventA={}, eventB={}, score изменен с {} на {}",
                        eventA, eventB, oldScore, updatedEventSimilarity.getScore());
                log.info("Полные данные обновленного сходства: {}", updatedEventSimilarity);
            } else {
                log.info("Создание новой записи о сходстве событий {} и {}", eventA, eventB);
                EventSimilarity newEventSimilarity = eventSimilarityMapper.toEntity(eventSimilarityAvro);

                if (newEventSimilarity == null) {
                    log.error("Маппер вернул null при преобразовании eventSimilarityAvro: {}", eventSimilarityAvro);
                    throw new IllegalStateException("Ошибка при маппинге EventSimilarityAvro в EventSimilarity");
                }

                EventSimilarity savedEventSimilarity = eventSimilarityRepository.save(newEventSimilarity);
                log.info("Создано новое сходство событий: eventA={}, eventB={}, score={}",
                        eventA, eventB, savedEventSimilarity.getScore());
                log.info("Полные данные нового сходства: {}", savedEventSimilarity);
            }
        } catch (Exception e) {
            log.error("Ошибка при сохранении сходства событий: {}", eventSimilarityAvro, e);
            throw e;
        }
    }
}