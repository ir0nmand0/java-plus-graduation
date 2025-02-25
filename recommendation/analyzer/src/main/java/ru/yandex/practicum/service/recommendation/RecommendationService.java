package ru.yandex.practicum.service.recommendation;

import ru.yandex.practicum.grpc.recommendation.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.recommendation.RecommendedEventProto;
import ru.yandex.practicum.grpc.recommendation.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.recommendation.UserPredictionsRequestProto;

import java.util.List;

/**
 * Интерфейс сервиса рекомендаций.
 * Предоставляет методы для получения различных типов рекомендаций событий.
 */
public interface RecommendationService {

    /**
     * Получает список рекомендованных событий для конкретного пользователя.
     *
     * @param request Запрос, содержащий данные пользователя и параметры для генерации рекомендаций
     * @return Список рекомендованных событий
     */
    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    /**
     * Получает список событий, похожих на указанное событие.
     *
     * @param request Запрос, содержащий идентификатор события и параметры для поиска похожих событий
     * @return Список похожих событий
     */
    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    /**
     * Получает список событий с указанием количества взаимодействий с ними.
     *
     * @param request Запрос, содержащий параметры для подсчета взаимодействий
     * @return Список событий с информацией о количестве взаимодействий
     */
    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}