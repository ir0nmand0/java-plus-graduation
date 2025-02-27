package ru.yandex.practicum.api.inbound.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.service.AggregatorService;

/**
 * Компонент, отвечающий за потребление событий пользовательских действий из Kafka.
 * Выступает точкой входа для данных о действиях пользователей, поступающих через систему обмена сообщениями Kafka.
 */
@Component
@RequiredArgsConstructor
public class ConsumerKafkaInComponent {
    /**
     * Сервис, который обрабатывает действия пользователей и агрегирует статистику.
     * Внедряется автоматически через конструктор благодаря аннотации @RequiredArgsConstructor.
     */
    private final AggregatorService aggregatorService;

    /**
     * Метод-слушатель Kafka, который потребляет события действий пользователей в формате Avro.
     * Этот метод запускается автоматически при поступлении сообщения в указанный топик.
     *
     * @param userActionAvro Данные о действии пользователя в формате Avro, полученные из Kafka
     * @KafkaListener указывает:
     * - topics: Топик Kafka для прослушивания ("stats.user-actions.v1")
     * - groupId: ID группы потребителей для этого слушателя ("aggregator-group")
     */
    @KafkaListener(topics = "stats.user-actions.v1", groupId = "aggregator-group")
    public void consumeUserActionAvro(UserActionAvro userActionAvro) {
        // Делегирование обработки полученного действия пользователя сервису-агрегатору
        aggregatorService.consumeUserActionAvro(userActionAvro);
    }
}