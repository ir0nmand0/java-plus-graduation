package ru.yandex.practicum.api.outbound.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * Компонент для отправки данных о схожести событий в Kafka.
 * Отвечает за исходящую коммуникацию с Kafka брокером.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTemplateOutComponent {
    /**
     * Шаблон для отправки сообщений в Kafka.
     * Специализирован для отправки сообщений типа EventSimilarityAvro с ключом типа String.
     * Внедряется автоматически через конструктор благодаря аннотации @RequiredArgsConstructor.
     */
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    /**
     * Отправляет сообщение в указанный топик Kafka.
     *
     * @param topic   Имя топика Kafka, в который будет отправлено сообщение
     * @param message Сообщение в формате Avro, содержащее данные о схожести событий
     */
    public void sendMessageKafka(String topic, EventSimilarityAvro message) {
        // Логирование информации о топике и содержимом отправляемого сообщения
        log.info("sendMessageKafka topic {} message {}", topic, message);

        // Отправка сообщения в указанный топик Kafka с использованием KafkaTemplate
        kafkaTemplate.send(topic, message);
    }
}