package ru.yandex.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.UserAction;


import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    /**
     * Находит ограниченное количество действий пользователя, отсортированных по времени в убывающем порядке.
     *
     * @param userId идентификатор пользователя
     * @param pageable объект для пагинации и ограничения количества результатов
     * @return ограниченный список действий пользователя, отсортированных по времени (сначала новые)
     */
    List<UserAction> findByUserIdOrderByTimestampDesc(@Param("userId") int userId, Pageable pageable);

    /**
     * Проверяет, взаимодействовал ли пользователь с определенным событием
     *
     * @param userId  Идентификатор пользователя
     * @param eventId Идентификатор события
     * @return true если взаимодействие было, иначе false
     */
    boolean existsByUserIdAndEventId(int userId, int eventId);

    /**
     * Находит идентификаторы всех событий, с которыми взаимодействовал пользователь
     *
     * @param userId Идентификатор пользователя
     * @return Список идентификаторов событий
     */
    @Query("SELECT usact.eventId FROM UserAction usact WHERE usact.userId = :userId")
    List<Integer> findInteractedEventsByUser(@Param("userId") int userId);

    /**
     * Подсчитывает количество взаимодействий с определенным событием
     *
     * @param eventId Идентификатор события
     * @return Количество взаимодействий
     */
    int countByEventId(int eventId);

    /**
     * Подсчитывает количество взаимодействий для списка событий за один запрос
     *
     * @param eventIds список идентификаторов событий
     * @return список пар [eventId, count]
     */
    @Query(value = "SELECT event_id, COUNT(*) as count " +
            "FROM user_actions " +
            "WHERE event_id IN :eventIds " +
            "GROUP BY event_id",
            nativeQuery = true)
    List<Object[]> countByEventIdIn(@Param("eventIds") List<Integer> eventIds);
}