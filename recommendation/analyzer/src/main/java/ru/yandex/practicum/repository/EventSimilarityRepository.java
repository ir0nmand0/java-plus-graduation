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

    /**
     * Находит события, похожие на указанное, с которыми заданный пользователь еще не взаимодействовал.
     */
    @Query(value = "SELECT * FROM event_similarities es " +
            "WHERE es.event_b = :eventId " +
            "AND es.event_a NOT IN " +
            "(SELECT event_id FROM user_actions WHERE user_id = :userId) " +
            "ORDER BY es.score DESC",
            nativeQuery = true)
    List<EventSimilarity> findSimilarEventsNotInteractedByUser(
            @Param("eventId") int eventId,
            @Param("userId") int userId);

    /**
     * Находит рекомендуемые события для пользователя на основе его недавних взаимодействий.
     */
    @Query(value =
            "WITH recent_user_events AS ( " +
                    "   SELECT event_id FROM user_actions " +
                    "   WHERE user_id = :userId " +
                    "   ORDER BY timestamp DESC " +
                    "   LIMIT :recentLimit " +
                    ") " +
                    "SELECT es.* FROM event_similarities es " +
                    "JOIN recent_user_events rue ON es.event_b = rue.event_id " +
                    "WHERE es.event_a NOT IN ( " +
                    "   SELECT event_id FROM user_actions WHERE user_id = :userId " +
                    ") " +
                    "ORDER BY es.score DESC " +
                    "LIMIT :maxResults",
            nativeQuery = true)
    List<EventSimilarity> findRecommendationsForUser(
            @Param("userId") int userId,
            @Param("recentLimit") int recentLimit,
            @Param("maxResults") int maxResults);


    /**
     * Находит события, похожие на указанное, с которыми заданный пользователь еще не взаимодействовал.
     * Включает лимит на количество результатов и сортировку по оценке сходства.
     *
     * @param eventId    идентификатор события, для которого ищутся похожие
     * @param userId     идентификатор пользователя, чьи взаимодействия нужно исключить
     * @param maxResults максимальное количество результатов
     * @return список объектов EventSimilarity с подходящими событиями
     */
    @Query(value = "SELECT * FROM event_similarities es " +
            "WHERE es.event_b = :eventId " +
            "AND es.event_a NOT IN " +
            "(SELECT event_id FROM user_actions WHERE user_id = :userId) " +
            "ORDER BY es.score DESC " +
            "LIMIT :maxResults",
            nativeQuery = true)
    List<EventSimilarity> findSimilarEventsNotInteractedByUserWithLimit(
            @Param("eventId") int eventId,
            @Param("userId") int userId,
            @Param("maxResults") int maxResults);
}