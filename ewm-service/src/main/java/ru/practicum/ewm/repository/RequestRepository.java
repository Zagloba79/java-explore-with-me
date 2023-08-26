package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.entity.EventConfirmedRequests;
import ru.practicum.ewm.entity.Request;
import ru.practicum.ewm.enums.Status;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> requestIds);

    List<Request> findAllByRequesterId(Long userId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    Long countByEventIdAndStatus(Long eventId, Status confirmed);

    @Query("SELECT new ru.practicum.main.model.EventConfirmedRequests(req.event.id , count(req.id)) " +
            "FROM Request AS req " +
            "WHERE req.event.id IN ?1 " +
            "AND req.status = 'CONFIRMED' " +
            "GROUP BY req.event.id ")
    List<EventConfirmedRequests> countByEventIds(List<Long> ids);
}
