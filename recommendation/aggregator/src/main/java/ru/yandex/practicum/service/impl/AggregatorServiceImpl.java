package ru.yandex.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.api.outbound.kafka.KafkaTemplateOutComponent;
import ru.yandex.practicum.model.EventData;
import ru.yandex.practicum.model.EventPair;
import ru.yandex.practicum.service.AggregatorService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Оптимизированная реализация сервиса-агрегатора, вычисляющего схожесть событий
 * на основе действий пользователей.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {
    private final KafkaTemplateOutComponent kafkaTemplate;

    // Хранилище данных о событиях
    private final Map<Long, EventData> eventsData = new ConcurrentHashMap<>();

    // Кеш для хранения сумм минимальных весов между парами событий
    private final Map<Long, Map<Long, Double>> minWeightsSumsCache = new ConcurrentHashMap<>();

    // Множество пар событий, для которых требуется пересчет сходства
    private final Set<EventPair> eventPairsToUpdate = ConcurrentHashMap.newKeySet();

    // Планировщик для асинхронного обновления сходства
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${stats.events-similarity.v1:stats.events-similarity.v1}")
    private String statsEventsSimilarityV1;

    @Value("${similarity.threshold:0.01}")
    private double similarityThreshold;

    private static final float VIEW_WEIGHT = 0.4F;
    private static final float LIKE_WEIGHT = 0.8F;
    private static final float REGISTER_WEIGHT = 1.0F;


    /**
     * Обрабатывает отложенные обновления сходства событий.
     */
    @Override
    @Scheduled(fixedRateString = "${similarity.calculation.period-seconds:60}000")
    public void processQueuedSimilarityUpdates() {
        int pairsCount = eventPairsToUpdate.size();
        if (pairsCount == 0) {
            return;
        }

        log.info("Начинаем пересчет сходства для {} пар событий", pairsCount);

        for (EventPair pair : eventPairsToUpdate) {
            try {
                updateSimilarity(pair.getEventA(), pair.getEventB());
            } catch (Exception e) {
                log.error("Ошибка при обновлении сходства для событий {} и {}",
                        pair.getEventA(), pair.getEventB(), e);
            }
        }

        eventPairsToUpdate.clear();
        log.info("Пересчет сходства завершен");
    }

    @Override
    public void consumeUserActionAvro(UserActionAvro userActionAvro) {
        log.info("Consume user action avro: {}", userActionAvro);

        float weight = switch (userActionAvro.getActionType()) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };

        processInteraction(userActionAvro.getEventId(), userActionAvro.getUserId(), weight);
    }

    /**
     * Обрабатывает взаимодействие пользователя с событием.
     * Отложенно добавляет пары событий для обновления сходства.
     */
    public void processInteraction(long eventId, long userId, float weight) {
        // Получаем данные о событии или создаем новые
        EventData eventData = eventsData.computeIfAbsent(eventId, k -> new EventData());

        // Обновляем вес взаимодействия пользователя с событием
        boolean updated = eventData.updateUserWeight(userId, weight);

        // Если вес был обновлен, добавляем все пары с данным событием в очередь на обновление
        if (updated) {
            for (Long otherEventId : eventsData.keySet()) {
                if (eventId != otherEventId) {
                    eventPairsToUpdate.add(new EventPair(eventId, otherEventId));
                }
            }
        }
    }

    /**
     * Обновляет метрику схожести между двумя событиями.
     */
    private void updateSimilarity(long eventA, long eventB) {
        // Проверяем наличие обоих событий в хранилище
        EventData dataA = eventsData.get(eventA);
        EventData dataB = eventsData.get(eventB);

        if (dataA == null || dataB == null) {
            return;
        }

        // Получаем (или вычисляем) сумму минимальных весов
        double sMin = getMinWeightSum(eventA, eventB);

        // Вычисляем косинусное сходство
        double similarity = calculateCosineSimilarity(dataA, dataB, sMin);

        // Отправляем результат в Kafka только если сходство превышает пороговое значение
        if (similarity >= similarityThreshold) {
            sendSimilarityToKafka(eventA, eventB, similarity, System.currentTimeMillis());
        }
    }

    /**
     * Отправляет информацию о схожести событий в Kafka.
     */
    private void sendSimilarityToKafka(long eventA, long eventB, double similarity, long timestamp) {
        EventSimilarityAvro message = new EventSimilarityAvro(eventA, eventB, (float) similarity, timestamp);
        kafkaTemplate.sendMessageKafka(statsEventsSimilarityV1, message);
    }

    /**
     * Получает или вычисляет сумму минимальных весов между парами событий.
     * Использует кеширование для оптимизации.
     */
    private double getMinWeightSum(long eventA, long eventB) {
        // Проверяем кеш
        Double cachedValue = minWeightsSumsCache
                .getOrDefault(eventA, Map.of())
                .get(eventB);

        if (cachedValue != null) {
            return cachedValue;
        }

        // Если значения нет в кеше, вычисляем его
        double sMin = calculateMinWeightSum(eventA, eventB);

        // Сохраняем результат в кеше
        minWeightsSumsCache
                .computeIfAbsent(eventA, k -> new ConcurrentHashMap<>())
                .put(eventB, sMin);

        return sMin;
    }

    /**
     * Вычисляет сумму минимальных весов взаимодействий.
     */
    private double calculateMinWeightSum(long eventA, long eventB) {
        Map<Long, Float> usersA = eventsData.get(eventA).getUserWeights();
        Map<Long, Float> usersB = eventsData.get(eventB).getUserWeights();

        double sMin = 0.0;

        // Оптимизируем цикл, используя меньшую из двух карт
        Map<Long, Float> smaller = usersA.size() <= usersB.size() ? usersA : usersB;
        Map<Long, Float> larger = smaller == usersA ? usersB : usersA;

        for (Map.Entry<Long, Float> entry : smaller.entrySet()) {
            long userId = entry.getKey();
            float weightSmaller = entry.getValue();
            Float weightLarger = larger.get(userId);

            if (weightLarger != null) {
                sMin += Math.min(weightSmaller, weightLarger);
            }
        }

        return sMin;
    }

    /**
     * Вычисляет косинусное сходство между двумя событиями.
     */
    private double calculateCosineSimilarity(EventData dataA, EventData dataB, double sMin) {
        double totalA = dataA.getTotalWeight();
        double totalB = dataB.getTotalWeight();

        // Предотвращение деления на ноль
        if (totalA <= 0 || totalB <= 0) {
            return 0.0;
        }

        return sMin / (Math.sqrt(totalA) * Math.sqrt(totalB));
    }
}