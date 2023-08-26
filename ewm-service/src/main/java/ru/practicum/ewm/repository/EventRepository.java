package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.enums.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategoryId(long catId);

    boolean existsByIdAndInitiatorId(long eventId, long userId);

    Set<Event> findAllByIdIn(List<Long> eventIds);

    @Query("SELECT event " +
            "FROM Event as event " +
            "WHERE (event.initiator.id in ?1 or ?1 is null) " +
            "AND (event.state in ?2 or ?2 is null) " +
            "AND (event.category.id in ?3 or ?3 is null) " +
            "AND (event.eventDate > ?4 or ?4 is null) " +
            "AND (event.eventDate < ?5 or ?5 is null) ")
    List<Event> findEventsByParams(List<Long> users, List<State> states, List<Long> categories,
                                   LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);
}