package ru.yandex.practicum.api.inbound.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang.SerializationException;
import org.apache.kafka.common.errors.ProducerFencedException;
import ru.yandex.practicum.grpc.recommendation.*;
import ru.yandex.practicum.service.recommendation.RecommendationService;

import java.util.List;

/**
 * Контроллер gRPC для обработки запросов рекомендаций событий.
 * Расширяет автоматически сгенерированный базовый класс для реализации методов сервиса рекомендаций.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationControllerGrpcExt extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    /**
     * Сервис, предоставляющий функциональность рекомендаций событий
     */
    private final RecommendationService recommendationService;

    /**
     * Обрабатывает запрос на получение рекомендаций для конкретного пользователя.
     *
     * @param request          Запрос, содержащий информацию о пользователе для генерации рекомендаций
     * @param responseObserver Наблюдатель для отправки результатов клиенту
     */
    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            // Получаем список рекомендованных событий для пользователя
            List<RecommendedEventProto> recommendationsForUser = recommendationService
                    .getRecommendationsForUser(request);

            // Отправляем каждое рекомендованное событие клиенту
            recommendationsForUser.forEach(responseObserver::onNext);

            // Сигнализируем о завершении передачи данных
            responseObserver.onCompleted();
        } catch (ProducerFencedException | SerializationException e) {
            // Логируем ошибку
            log.error(e.getMessage(), e);

            // Отправляем клиенту сообщение об ошибке с соответствующим статусом
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    /**
     * Обрабатывает запрос на получение похожих событий для указанного события.
     *
     * @param request          Запрос, содержащий информацию о событии для поиска похожих
     * @param responseObserver Наблюдатель для отправки результатов клиенту
     */
    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            // Получаем список похожих событий
            List<RecommendedEventProto> similarEvents = recommendationService
                    .getSimilarEvents(request);

            // Отправляем каждое похожее событие клиенту
            similarEvents.forEach(responseObserver::onNext);

            // Сигнализируем о завершении передачи данных
            responseObserver.onCompleted();
        } catch (ProducerFencedException | SerializationException e) {
            // Логируем ошибку
            log.error(e.getMessage(), e);

            // Отправляем клиенту сообщение об ошибке с соответствующим статусом
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    /**
     * Обрабатывает запрос на получение количества взаимодействий с событиями.
     *
     * @param request          Запрос с параметрами для подсчета взаимодействий
     * @param responseObserver Наблюдатель для отправки результатов клиенту
     */
    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            // Получаем список событий с количеством взаимодействий
            List<RecommendedEventProto> interactionsCount = recommendationService
                    .getInteractionsCount(request);

            // Отправляем информацию о каждом событии клиенту
            interactionsCount.forEach(responseObserver::onNext);

            // Сигнализируем о завершении передачи данных
            responseObserver.onCompleted();
        } catch (ProducerFencedException | SerializationException e) {
            // Логируем ошибку
            log.error(e.getMessage(), e);

            // Отправляем клиенту сообщение об ошибке с соответствующим статусом
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}