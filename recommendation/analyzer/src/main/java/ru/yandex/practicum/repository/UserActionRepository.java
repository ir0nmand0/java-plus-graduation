package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.UserAction;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    List<UserAction> findByUserIdOrderByTimestampDesc(@Param("userId") int userId);

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
}