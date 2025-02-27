// Файл: ru/yandex/practicum/model/EventData.java
package ru.yandex.practicum.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Модель для хранения всех данных, связанных с событием.
 * Содержит веса взаимодействий пользователей и суммарные метрики.
 */
public class EventData {
    // Веса взаимодействий пользователей с событием
    private final Map<Long, Float> userWeights = new ConcurrentHashMap<>();

    // Суммарный вес всех взаимодействий
    private double totalWeight = 0.0;

    // Флаг, указывающий на необходимость пересчета весов
    private volatile boolean needsRecalculation = false;

    /**
     * Добавляет или обновляет вес взаимодействия пользователя с событием.
     * Обновление происходит только если новый вес больше старого.
     *
     * @param userId ID пользователя
     * @param weight Вес взаимодействия
     * @return true, если вес был обновлен, false в противном случае
     */
    public boolean updateUserWeight(long userId, float weight) {
        Float oldWeight = userWeights.get(userId);
        // Обновляем вес пользователя только если новый вес больше
        if (oldWeight == null || weight > oldWeight) {
            userWeights.put(userId, weight);
            needsRecalculation = true;
            return true;
        }
        return false;
    }

    /**
     * Возвращает неизменяемую копию карты весов пользователей.
     *
     * @return Карта весов пользователей (ID пользователя -> вес)
     */
    public Map<Long, Float> getUserWeights() {
        return Map.copyOf(userWeights);
    }

    /**
     * Пересчитывает суммарный вес всех взаимодействий.
     *
     * @return Пересчитанное значение суммарного веса
     */
    public double recalculateTotalWeight() {
        if (needsRecalculation) {
            totalWeight = userWeights.values().stream()
                    .mapToDouble(weight -> weight)
                    .sum();
            needsRecalculation = false;
        }
        return totalWeight;
    }

    /**
     * Возвращает суммарный вес всех взаимодействий.
     * При необходимости выполняет пересчет.
     *
     * @return Суммарный вес всех взаимодействий
     */
    public double getTotalWeight() {
        return needsRecalculation ? recalculateTotalWeight() : totalWeight;
    }
}