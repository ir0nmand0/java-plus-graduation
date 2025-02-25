package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с данными о схожести событий.
 * Предоставляет методы для поиска и анализа похожих событий.
 */
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    /**
     * Находит запись о схожести между двумя конкретными событиями
     *
     * @param eventA Идентификатор первого события
     * @param eventB Идентификатор второго события
     * @return Опциональный объект с информацией о схожести событий
     */
    Optional<EventSimilarity> findByEventAAndEventB(long eventA, long eventB);

    /**
     * Находит похожие события на указанное событие,
     * отсортированные по убыванию оценки схожести
     *
     * @param eventId Идентификатор события, для которого ищутся похожие
     * @return Список похожих событий с оценками схожести
     */
    @Query("SELECT e.eventB, e.score " +
            "FROM EventSimilarity e " +
            "WHERE e.eventA = :eventId " +
            "ORDER BY e.score DESC")
    List<EventSimilarity> findRawSimilarEvents(@Param("eventId") long eventId);
}