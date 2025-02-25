// Файл: ru/yandex/practicum/model/EventPair.java
package ru.yandex.practicum.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Модель для представления пары событий.
 * Используется для кеширования и отложенного обновления сходства.
 */
@Getter
@EqualsAndHashCode
public class EventPair {
    private final long eventA;
    private final long eventB;

    /**
     * Создает новую пару событий, гарантируя, что меньший ID всегда будет первым.
     *
     * @param eventA ID первого события
     * @param eventB ID второго события
     */
    public EventPair(long eventA, long eventB) {
        // Гарантируем, что меньший ID всегда будет первым
        this.eventA = Math.min(eventA, eventB);
        this.eventB = Math.max(eventA, eventB);
    }
}