package ru.yandex.practicum.service.recommendation.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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

import java.util.*;
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
        int recentLimit = 20; // Количество недавних событий пользователя для анализа, можно настроить параметром

        log.info("Получение рекомендаций для пользователя userId={}, maxResults={}", userId, maxResults);

        try {
            // Используем новый оптимизированный метод, который выполняет всю логику в одном SQL запросе
            List<EventSimilarity> recommendedEvents = eventSimilarityRepository
                    .findRecommendationsForUser(userId, recentLimit, maxResults);

            log.info("Найдено {} рекомендуемых событий для пользователя userId={}",
                    recommendedEvents.size(), userId);

            // Если рекомендации не найдены, возвращаем пустой список
            if (recommendedEvents.isEmpty()) {
                log.info("Не найдены рекомендации для пользователя userId={}, возвращаем пустой список", userId);
                return Collections.emptyList();
            }

            // Преобразуем результат в формат ответа
            List<RecommendedEventProto> recommendations = recommendedEvents.stream()
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

        log.info("Получение похожих событий для eventId={}, userId={}, maxResults={}",
                eventId, userId, maxResults);

        try {
            // Используем новый оптимизированный метод, выполняющий всю логику в одном SQL запросе
            List<EventSimilarity> similarEvents = eventSimilarityRepository
                    .findSimilarEventsNotInteractedByUserWithLimit(eventId, userId, maxResults);

            log.info("Найдено {} похожих событий для eventId={}, с которыми пользователь userId={} не взаимодействовал",
                    similarEvents.size(), eventId, userId);

            // Преобразуем результат в формат ответа
            List<RecommendedEventProto> result = similarEvents.stream()
                    .map(eventSimilarity -> RecommendedEventProto.newBuilder()
                            .setEventId(eventSimilarity.getEventA())
                            .setScore(eventSimilarity.getScore())
                            .build())
                    .toList();

            return result;
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

        log.info("Получение количества взаимодействий для {} событий", eventIds.size());

        try {
            // Если список пуст, возвращаем пустой результат
            if (eventIds.isEmpty()) {
                return Collections.emptyList();
            }

            // Получаем количество взаимодействий за один запрос
            List<Object[]> countsData = userActionRepository.countByEventIdIn(eventIds);

            // Создаем мапу eventId -> count для быстрого доступа
            Map<Integer, Long> countMap = new HashMap<>();
            for (Object[] row : countsData) {
                int eventId = ((Number) row[0]).intValue();
                long count = ((Number) row[1]).longValue();
                countMap.put(eventId, count);
            }

            // Формируем результат, включая события с нулевым количеством взаимодействий
            List<RecommendedEventProto> result = eventIds.stream()
                    .map(eventId -> RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(countMap.getOrDefault(eventId, 0L).intValue())
                            .build())
                    .toList();

            log.info("Успешно получено количество взаимодействий для {} событий", eventIds.size());
            return result;
        } catch (Exception e) {
            log.error("Ошибка при получении количества взаимодействий для событий: {}", eventIds, e);
            throw e;
        }
    }
}