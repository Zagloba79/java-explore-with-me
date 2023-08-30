package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.enums.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiator_Id(long userId, Pageable pageable);

    boolean existsByCategoryId(long catId);

    boolean existsByIdAndInitiatorId(long eventId, long userId);

    Set<Event> findAllByIdIn(List<Long> eventIds);

    @Query("SELECT event " +
            "FROM Event as event " +
            "WHERE (event.initiator.id IN ?1 OR ?1 is null) " +
            "AND (event.state IN ?2 OR ?2 IS null) " +
            "AND (event.category.id IN ?3 OR ?3 IS null) " +
            "AND (event.eventDate > ?4 OR ?4 IS null) " +
            "AND (event.eventDate < ?5 OR ?5 IS null) ")
    List<Event> findEventsByParamsForAdmin(List<Long> users, List<State> states, List<Long> categories,
                                           LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

    @Query("SELECT event " +
            "FROM Event as event " +
            "WHERE ((?1 is null) OR ((lower(event.annotation) LIKE CONTACT('%', lower(?1), '%')) OR " +
            "(lower(event.description) LIKE CONTACT('%', lower(?1), '%')))) " +
            "AND (event.category.id IN ?2 OR ?2 IS null) " +
            "AND (event.paid = ?3 OR ?3 IS null) " +
            "AND (event.eventDate > ?4 OR ?4 IS null) AND (event.eventDate < ?5 OR ?5 IS null) " +
            "AND (?6 = false OR ((?6 = true and event.participantLimit > " +
            "(SELECT count(*) FROM Request AS r WHERE event.id = r.event.id))) " +
            "OR (event.participantLimit > 0 )) " +
            "AND event.state = 'PUBLISHED'")
    List<Event> findEventsByParamsForEverybody(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, Boolean onlyAvailable);
}