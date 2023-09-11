package ru.practicum.ewm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.StatClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.ewm.dto.EventConfirmedRequests;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.exception.StatException;
import ru.practicum.ewm.repository.RequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class InfoFromRep {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatClient statClient;
    private final RequestRepository requestRepository;
    private final ObjectMapper objectMapper;

    public Map<Long, Long> getViewsFromStat(List<Event> events) {
        Map<Long, Long> mapView = new HashMap<>();
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        if (start == null) {
            return Map.of();
        }
        String uris = events.stream()
                .map(ev -> "/events/" + ev.getId())
                .collect(Collectors.joining(","));
        ResponseEntity<Object> response = statClient.getStats(start.format(FORMATTER),
                LocalDateTime.now().format(FORMATTER), uris, true);
        try {
            List<ViewStatsDto> viewStats = Arrays.asList(objectMapper.readValue(
                    objectMapper.writeValueAsString(response.getBody()), ViewStatsDto[].class));
            viewStats.forEach(statistic -> mapView.put(
                            Long.parseLong(statistic.getUri().replaceAll("\\D+", "")),
                            statistic.getHits()
                    )
            );
        } catch (Exception e) {
            throw new StatException("Произошла ошибка выполнения запроса статистики", e);
        }
        return mapView;
    }

    public Map<Long, Long> getConfirmedRequestsFromRep(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(toList());
        List<EventConfirmedRequests> eventConfirmedRequests =
                requestRepository.getCountOfConfirmedRequestsByEventId(eventIds);
        return eventConfirmedRequests.stream()
                .collect(toMap(EventConfirmedRequests::getEvent, EventConfirmedRequests::getConfirmedRequestsCount));
    }
}