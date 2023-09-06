package ru.practicum.ewm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.ewm.dto.EventConfirmedRequests;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.exception.StatException;
import ru.practicum.ewm.repository.RequestRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class EwmStatServiceImpl implements EwmStatService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatClient statClient;
    private final RequestRepository requestRepository;
    private final ObjectMapper objectMapper;
    private final String app = "ewm_service";

    @Override
    @Transactional
    public void saveEndpointHit(HttpServletRequest request) {
        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statClient.create(endpointHit);
    }

    @Override
    public Map<Long, Long> getViewsFromStat(List<Event> events) {
        Map<Long, Long> mapView = new HashMap<>();
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        if (start == null) {
            return Map.of();
        }
        List<String> uris = events.stream()
                .map(ev -> "/events/" + ev.getId())
                .collect(toList());
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
        } catch (JsonProcessingException e) {
            throw new StatException("Произошла ошибка выполнения запроса статистики");
        }
        return mapView;
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsFromStat(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(toList());
        List<EventConfirmedRequests> eventConfirmedRequests =
                requestRepository.getCountOfConfirmedRequestsByEventId(eventIds);
        return eventConfirmedRequests.stream()
                .collect(toMap(EventConfirmedRequests::getEvent, EventConfirmedRequests::getConfirmedRequestsCount));
    }
}
