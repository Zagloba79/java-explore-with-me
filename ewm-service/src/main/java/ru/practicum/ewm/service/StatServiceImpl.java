package ru.practicum.ewm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatClient;
import ru.practicum.ewm.repository.RequestRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RequestRepository requestRepository;

    private final StatClient statClient;

    private final ObjectMapper objectMapper;

//    @Override
//    public Map<Long, Long> createMapOfEventsWithCountOfConfirmedRequests(List<Event> events) {
//        List<Long> eventIds = events.stream().map(Event::getId).collect(toList());
//        List<EventConfirmedRequests> eventConfirmedRequests = requestRepository.findCountsByEventIds(eventIds);
//        return eventConfirmedRequests.stream()
//                .collect(toMap(EventConfirmedRequests::getEventId, EventConfirmedRequests::getConfirmedRequestsCount));
//    }
//
//    @Override
//    public Map<Long, Long> createMapOfEventsWithCountOfViews(List<Event> events) {
//        Map<Long, Long> mapOfViews = new HashMap<>();
//        LocalDateTime start = events.stream().map(Event::getCreatedOn).min(LocalDateTime::compareTo).orElse(null);
//        if (start == null) {
//            return Map.of();
//        }
//        List<String> uris = events.stream().map(event -> "/events/" + event.getId()).collect(toList());
//        ResponseEntity<Object> response = statClient.getStats(start.format(FORMATTER),
//                LocalDateTime.now().format(FORMATTER), uris, true);
//        try {
//            List<ViewStatsDto> stats = Collections.singletonList(objectMapper.readValue(
//                    objectMapper.writeValueAsString(response.getBody()), ViewStatsDto.class));
//            stats.forEach(statistic -> mapOfViews.put(
//                            Long.parseLong(statistic.getUri().replaceAll("\\D+", "")),
//                            statistic.getHits()
//                    )
//            );
//        } catch (JsonProcessingException e) {
//            throw new StatException("Произошла ошибка выполнения запроса статистики");
//        }
//        return mapOfViews;
//    }

    @Transactional
    @Override
    public void saveEndpointHit(HttpServletRequest request, String serviceName) {
        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .app(serviceName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statClient.create(endpointHit);
    }
}