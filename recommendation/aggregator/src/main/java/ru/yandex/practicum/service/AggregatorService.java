package ru.yandex.practicum.service;

import org.springframework.scheduling.annotation.Scheduled;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Интерфейс сервиса агрегации данных о действиях пользователей.
 * Определяет основные операции для обработки пользовательских действий,
 * поступающих в виде сообщений в формате Avro.
 */
public interface AggregatorService {

    @Scheduled(fixedRateString = "${similarity.calculation.period-seconds:60}000")
    void processQueuedSimilarityUpdates();

    /**
     * Обрабатывает действие пользователя, полученное в формате Avro.
     * Этот метод вызывается при получении сообщения из Kafka
     * и запускает процесс анализа и агрегации данных.
     *
     * @param userActionAvro Объект, содержащий информацию о действии пользователя
     *                       (идентификатор пользователя, идентификатор события,
     *                       тип действия и т.д.) в формате Avro
     */
    void consumeUserActionAvro(UserActionAvro userActionAvro);
}