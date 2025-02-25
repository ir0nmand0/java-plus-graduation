package ru.yandex.practicum.service.recommendation.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.recommendation.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.recommendation.RecommendedEventProto;
import ru.yandex.practicum.grpc.recommendation.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.recommendation.UserPredictionsRequestProto;
import ru.yandex.practicum.model.EventSimilarity;
import ru.yandex.practicum.model.UserAction;
import ru.yandex.practicum.repository.EventSimilarityRepository;
import ru.yandex.practicum.repository.UserActionRepository;
import ru.yandex.practicum.service.recommendation.RecommendationService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса рекомендаций.
 * Класс отвечает за формирование рекомендаций событий на основе действий пользователей
 * и данных о сходстве между событиями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {
    /**
     * Репозиторий для работы с данными о сходстве событий
     */
    private final EventSimilarityRepository eventSimilarityRepository;

    /**
     * Репозиторий для работы с данными о действиях пользователей
     */
    private final UserActionRepository userActionRepository;

    /**
     * Получает список рекомендованных событий для конкретного пользователя.
     * Метод анализирует недавние взаимодействия пользователя и на их основе
     * подбирает похожие события, с которыми пользователь еще не взаимодействовал.
     *
     * @param request Запрос, содержащий идентификатор пользователя и максимальное количество результатов
     * @return Список рекомендованных событий с оценками релевантности
     */
    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        int userId = request.getUserId();
        int maxResults = request.getMaxResults();

        log.debug("Получение рекомендаций для пользователя userId={}, maxResults={}", userId, maxResults);

        try {
            // Получаем недавние события, с которыми взаимодействовал пользователь
            List<Integer> recentlyInteractedEvents = userActionRepository
                    .findByUserIdOrderByTimestampDesc(userId)
                    .stream()
                    .limit(maxResults)
                    .map(UserAction::getEventId)
                    .toList();

            log.debug("Найдено {} событий, с которыми недавно взаимодействовал пользователь userId={}",
                    recentlyInteractedEvents.size(), userId);

            if (recentlyInteractedEvents.isEmpty()) {
                log.info("Не найдены взаимодействия пользователя userId={}, возвращаем пустой список рекомендаций", userId);
                return Collections.emptyList();
            }

            // Находим похожие события, с которыми пользователь еще не взаимодействовал
            List<RecommendedEventProto> recommendations = recentlyInteractedEvents
                    .stream()
                    .flatMap(eventId -> {
                        List<EventSimilarity> similarEvents = eventSimilarityRepository.findRawSimilarEvents(eventId);
                        log.debug("Найдено {} похожих событий для eventId={}", similarEvents.size(), eventId);
                        return similarEvents.stream();
                    })
                    .filter(event -> {
                        boolean userHasNotInteracted = !userActionRepository
                                .existsByUserIdAndEventId(userId, event.getEventA());
                        return userHasNotInteracted;
                    })
                    .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                    .limit(maxResults)
                    .map(eventSimilarity -> RecommendedEventProto.newBuilder()
                            .setEventId(eventSimilarity.getEventA())
                            .setScore(eventSimilarity.getScore())
                            .build())
                    .collect(Collectors.toList());

            log.info("Сформированы рекомендации для пользователя userId={}: найдено {} рекомендованных событий",
                    userId, recommendations.size());

            return recommendations;
        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций для пользователя userId={}", userId, e);
            throw e;
        }
    }

    /**
     * Получает список событий, похожих на указанное событие.
     * Метод исключает из результатов события, с которыми пользователь уже взаимодействовал.
     *
     * @param request Запрос, содержащий идентификатор события, идентификатор пользователя и максимальное количество результатов
     * @return Список похожих событий с оценками сходства
     */
    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        int eventId = request.getEventId();
        int userId = request.getUserId();
        int maxResults = request.getMaxResults();

        log.debug("Получение похожих событий для eventId={}, userId={}, maxResults={}",
                eventId, userId, maxResults);

        try {
            // Получаем все похожие события для заданного идентификатора события
            List<EventSimilarity> similarEventsData = eventSimilarityRepository.findRawSimilarEvents(eventId);
            log.debug("Найдено {} похожих событий для eventId={}", similarEventsData.size(), eventId);

            List<RecommendedEventProto> similarEvents = similarEventsData
                    .stream()
                    .map(eventSimilarity -> RecommendedEventProto.newBuilder()
                            .setEventId(eventSimilarity.getEventA())
                            .setScore(eventSimilarity.getScore())
                            .build())
                    .toList();

            // Исключаем события, с которыми пользователь уже взаимодействовал
            List<Integer> userInteractions = userActionRepository.findInteractedEventsByUser(userId);
            log.debug("Пользователь userId={} взаимодействовал с {} событиями", userId, userInteractions.size());

            List<RecommendedEventProto> filteredEvents = similarEvents
                    .stream()
                    .filter(event -> !userInteractions.contains(event.getEventId()))
                    .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
                    .limit(maxResults)
                    .toList();

            log.info("Найдено {} похожих событий для eventId={} для пользователя userId={} после фильтрации",
                    filteredEvents.size(), eventId, userId);

            return filteredEvents;
        } catch (Exception e) {
            log.error("Ошибка при получении похожих событий для eventId={}, userId={}", eventId, userId, e);
            throw e;
        }
    }

    /**
     * Получает количество взаимодействий для списка событий.
     *
     * @param request Запрос, содержащий список идентификаторов событий
     * @return Список событий с количеством взаимодействий в поле score
     */
    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Integer> eventIds = request.getEventIdsList();

        log.debug("Получение количества взаимодействий для {} событий", eventIds.size());

        try {
            // Получаем количество взаимодействий для каждого события
            List<RecommendedEventProto> result = eventIds
                    .stream()
                    .map(eventId -> {
                        int interactionCount = userActionRepository.countByEventId(eventId);
                        log.debug("Для события eventId={} найдено {} взаимодействий", eventId, interactionCount);

                        return RecommendedEventProto.newBuilder()
                                .setEventId(eventId)
                                .setScore(interactionCount)
                                .build();
                    })
                    .toList();

            log.info("Успешно получено количество взаимодействий для {} событий", eventIds.size());
            return result;
        } catch (Exception e) {
            log.error("Ошибка при получении количества взаимодействий для событий: {}", eventIds, e);
            throw e;
        }
    }
}