package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.model.ActionType;
import ru.yandex.practicum.model.UserAction;

import java.time.Instant;

/**
 * Маппер для преобразования Avro-объектов действий пользователя в доменные сущности.
 * <p>
 * Примечание: Используем кастомные методы вместо expression из-за ограничений MapStruct.
 * При использовании выражений вида expression = "java(...)" MapStruct не добавляет
 * необходимые импорты для классов, используемых только в этих выражениях (Instant, ActionType),
 * что приводит к ошибкам компиляции в сгенерированном коде.
 * <p>
 * Определение методов непосредственно в интерфейсе решает эту проблему, поскольку
 * MapStruct правильно добавляет импорты для всех типов, используемых в сигнатурах методов.
 */
@Mapper(componentModel = "spring")
public interface UserActionMapper {

    @Mapping(target = "actionType", source = "actionType", qualifiedByName = "toActionType")
    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "toInstant")
    @Mapping(target = "id", ignore = true)
    UserAction toEntity(UserActionAvro source);

    /**
     * Конвертирует ActionTypeAvro в ActionType
     * <p>
     * Этот метод заменяет выражение: expression = "java(ActionType.valueOf(source.getActionType().name()))"
     * и гарантирует правильный импорт класса ActionType в сгенерированном коде.
     */
    @Named("toActionType")
    default ActionType toActionType(ru.practicum.ewm.stats.avro.ActionTypeAvro actionTypeAvro) {
        return ActionType.valueOf(actionTypeAvro.name());
    }

    /**
     * Конвертирует long timestamp в java.time.Instant
     * <p>
     * Этот метод заменяет выражение: expression = "java(Instant.ofEpochSecond(source.getTimestamp()))"
     * и гарантирует правильный импорт класса java.time.Instant в сгенерированном коде.
     */
    @Named("toInstant")
    default Instant toInstant(long timestamp) {
        return Instant.ofEpochSecond(timestamp);
    }
}