package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.dto.EventConfirmedRequests;
import ru.practicum.ewm.entity.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    @Query("SELECT new ru.practicum.ewm.dto.EventConfirmedRequests(req.event.id , count(req.id)) " +
            "FROM Request AS req " +
            "WHERE req.event.id IN ?1 " +
            "AND req.status = 'CONFIRMED' " +
            "GROUP BY req.event.id ")
    List<EventConfirmedRequests> findCountsByEventIds(List<Long> longs);
}
