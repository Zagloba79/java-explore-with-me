package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.model.ViewStats(eh.app, eh.uri, COUNT(DISTINCT eh.ip)) " +
            "FROM EndpointHit eh " +
            "WHERE eh.uri IN (?1) " +
            "AND eh.timestamp BETWEEN ?2 and ?3 " +
            "GROUP BY eh.ip, eh.app, eh.uri " +
            "ORDER BY COUNT(distinct eh.ip) desc ")
    List<ViewStats> getUniqueHitsListByUris(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStats(eh.app, eh.uri, COUNT(DISTINCT eh.ip)) " +
            "FROM EndpointHit eh " +
            "WHERE eh.timestamp BETWEEN ?1 and ?2 " +
            "GROUP BY eh.ip, eh.app, eh.uri " +
            "ORDER BY COUNT(distinct eh.ip) desc ")
    List<ViewStats> getUniqueHitsList(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStats(eh.app, eh.uri, COUNT(*)) " +
            "FROM EndpointHit eh " +
            "WHERE eh.uri IN (?1) " +
            "AND eh.timestamp BETWEEN ?2 and ?3 " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(eh.ip) desc ")
    List<ViewStats> getHitsListByUris(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStats(eh.app, eh.uri, COUNT(*)) " +
            "FROM EndpointHit eh " +
            "WHERE eh.timestamp BETWEEN ?1 and ?2 " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(eh.ip) desc ")
    List<ViewStats> getHitsList(LocalDateTime start, LocalDateTime end);
}