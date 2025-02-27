package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Сущность, представляющая действие пользователя с событием.
 * Хранит информацию о пользователе, событии, типе взаимодействия и времени его совершения.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_actions")
public class UserAction {

    /**
     * Уникальный идентификатор действия пользователя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Идентификатор пользователя, совершившего действие
     */
    @Column(nullable = false)
    private int userId;

    /**
     * Идентификатор события, с которым взаимодействовал пользователь
     */
    @Column(nullable = false)
    private int eventId;

    /**
     * Тип совершенного действия (просмотр, регистрация, лайк)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    /**
     * Временная метка, когда было совершено действие
     */
    @Column(nullable = false)
    private Instant timestamp;
}