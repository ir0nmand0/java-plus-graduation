package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Сущность, представляющая метрику схожести между двумя событиями.
 * Хранит информацию о паре событий, оценке их схожести и времени вычисления.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_similarities")
public class EventSimilarity {

    /**
     * Уникальный идентификатор записи о схожести
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Идентификатор первого события в паре
     */
    @Column(nullable = false)
    private int eventA;

    /**
     * Идентификатор второго события в паре
     */
    @Column(nullable = false)
    private int eventB;

    /**
     * Оценка схожести между событиями (от 0 до 1)
     */
    @Column(nullable = false)
    private float score;

    /**
     * Временная метка, когда была рассчитана схожесть
     */
    @Column(nullable = false)
    private Instant timestamp;
}