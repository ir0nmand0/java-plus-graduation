package ru.yandex.practicum.service.analyzer;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Интерфейс сервиса анализа данных.
 * Предоставляет методы для сохранения информации о действиях пользователей
 * и сходстве между событиями.
 */
public interface AnalyzerService {

    /**
     * Сохраняет информацию о действии пользователя.
     * Действия пользователей могут включать просмотры, лайки, бронирования и другие
     * взаимодействия с событиями в системе.
     *
     * @param userActionAvro Объект, содержащий данные о действии пользователя в формате Avro
     */
    void saveUserAction(UserActionAvro userActionAvro);

    /**
     * Сохраняет информацию о сходстве между событиями.
     * Эти данные могут использоваться для создания рекомендаций и
     * предложения пользователям похожих событий.
     *
     * @param eventSimilarityAvro Объект, содержащий данные о сходстве событий в формате Avro
     */
    void saveEventSimilarity(EventSimilarityAvro eventSimilarityAvro);
}