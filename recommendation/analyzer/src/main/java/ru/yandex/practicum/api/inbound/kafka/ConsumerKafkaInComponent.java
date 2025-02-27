package ru.yandex.practicum.api.inbound.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.service.analyzer.AnalyzerService;

/**
 * Компонент для приема и обработки сообщений из Kafka.
 * Слушает определенные топики и перенаправляет полученные сообщения в сервис-анализатор.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerKafkaInComponent {

    /**
     * Сервис для анализа и сохранения данных о действиях пользователей и схожести событий
     */
    private final AnalyzerService analyzerService;

    /**
     * Обрабатывает сообщения о действиях пользователей из Kafka топика.
     *
     * @param userActionAvro Десериализованные данные о действии пользователя в формате Avro
     */
    @KafkaListener(topics = "stats.user-actions.v1", groupId = "analyzer-group")
    public void consumeUserActionAvro(UserActionAvro userActionAvro) {
        // Логируем полученное сообщение для отладки
        log.info("consumeUserActionAvro UserActionAvro: {}", userActionAvro);

        // Делегируем обработку полученных данных сервису-анализатору
        analyzerService.saveUserAction(userActionAvro);
    }

    /**
     * Обрабатывает сообщения о схожести событий из Kafka топика.
     *
     * @param eventSimilarityAvro Десериализованные данные о схожести событий в формате Avro
     */
    @KafkaListener(topics = "stats.events-similarity.v1", groupId = "analyzer-group")
    public void consumeEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        // Логируем полученное сообщение для отладки
        log.info("consumeEventSimilarity EventSimilarityAvro: {}", eventSimilarityAvro);

        // Делегируем обработку полученных данных сервису-анализатору
        analyzerService.saveEventSimilarity(eventSimilarityAvro);
    }
}