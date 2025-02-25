package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.model.EventSimilarity;

import java.time.Instant;

/**
 * Маппер для преобразования Avro-объектов в доменные сущности.
 * <p>
 * Примечание: Используем кастомные методы вместо expression, потому что MapStruct
 * имеет проблему с импортом классов, используемых только в expression.
 * В сгенерированном коде MapStruct не добавляет import для классов, используемых
 * только внутри java-выражений (например, java.time.Instant), что приводит к ошибкам компиляции.
 * Объявляя методы напрямую в интерфейсе, мы гарантируем, что MapStruct корректно
 * импортирует все необходимые классы в сгенерированную реализацию.
 */
@Mapper(componentModel = "spring")
public interface EventSimilarityMapper {

    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "toInstant")
    @Mapping(target = "id", ignore = true)
    EventSimilarity toEntity(EventSimilarityAvro source);

    /**
     * Конвертирует long timestamp в java.time.Instant
     * <p>
     * Этот метод используется вместо expression = "java(Instant.ofEpochSecond(source.getTimestamp()))",
     * чтобы гарантировать корректный импорт класса java.time.Instant в сгенерированном коде.
     */
    @Named("toInstant")
    default Instant toInstant(long timestamp) {
        return Instant.ofEpochSecond(timestamp);
    }
}