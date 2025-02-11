package com.github.artemlv.ewm.event.storage.database;

import com.github.artemlv.ewm.event.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event>,
        JpaSpecificationExecutor<Event> {

    List<Event> findByInitiatorId(final long userId, final Pageable pageable);

    List<Event> findByCategoryId(final long catId);

    @Query(value = "SELECT e.* FROM events e JOIN locations l "
            + "ON e.location_id = l.id "
            + "WHERE e.state = 'PUBLISHED' AND distance(l.lat, l.lon, :lat, :lon) < :radius", nativeQuery = true)
    List<Event> findByLocationLatAndLocationLonAndLocationRadius(@Param("lat") final double lat,
                                                                 @Param("lon") final double lon,
                                                                 @Param("radius") final double radius);

    @Query(value = "SELECT e.* FROM events e JOIN locations l "
            + "ON e.location_id = l.id "
            + "WHERE e.state = 'PUBLISHED' "
            + "AND (distance(l.lat, l.lon, :lat, :lon) < l.radius "
            + "OR (l.lat = :lat and l.lon = :lon))", nativeQuery = true)
    List<Event> findByLocationLatAndLocationLon(@Param("lat") final double lat, @Param("lon") final double lon);
}
