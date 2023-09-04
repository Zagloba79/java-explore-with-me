package ru.practicum.ewm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatClient;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entity.*;
import ru.practicum.ewm.exception.StatException;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.model.ViewStats;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static ru.practicum.ewm.enums.State.PUBLISHED;

@Service
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService {
    private final CategoryRepository categoriesRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final StatClient statClient;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        List<Category> categories = categoriesRepository.findAll(pageable).toList();
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream().map(CategoryMapper::toCategoryDto).collect(toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        final Category category = categoriesRepository.findById(catId)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public List<EventShortDto> getAllEvents(String text, List<Long> categories, Boolean paid,
                                            String rangeStart, String rangeEnd,
                                            Boolean onlyAvailable, String sort, int from, int size,
                                            HttpServletRequest request) {
        LocalDateTime startTime;
        LocalDateTime endTime;
        if (rangeStart == null) {
            startTime = LocalDateTime.now();
        } else {
            startTime = LocalDateTime.parse(rangeStart, FORMATTER);
        }
        if (rangeEnd == null) {
            endTime = LocalDateTime.now().plusYears(10);
        } else {
            endTime = LocalDateTime.parse(rangeEnd, FORMATTER);
        }
        if (startTime.isAfter(endTime)) {
            throw new ValidationException("Даты попутаны");
        }
        sort = sort.equalsIgnoreCase("event_date") ? "eventDate" : sort;
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(sort).descending());
        List<Event> events = eventRepository.getAllByParam(text, categories, paid, startTime,
                endTime, onlyAvailable, pageable);
        Map<Long, Long> views = getViewsFromStatService(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequestsFromStatService(events);
        List<EventShortDto> eventShorts = new ArrayList<>();
        for (Event event : events) {
            EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
            eventShortDto.setViews(views.get(eventShortDto.getId()));
            eventShortDto.setConfirmedRequests(confirmedRequests.get(eventShortDto.getId()));
            eventShorts.add(eventShortDto);
        }
        eventRepository.saveAll(events);
        saveEndpointHit(request);
        return eventShorts;
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (!event.getState().equals(PUBLISHED)) {
            throw new ObjectNotFoundException("Event is not published");
        }
        Map<Long, Long> views = getViewsFromStatService(List.of(event));
        Map<Long, Long> confirmedRequests = getConfirmedRequestsFromStatService(List.of(event));
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(views.get(eventFullDto.getId()));
        eventFullDto.setConfirmedRequests(confirmedRequests.get(eventFullDto.getId()));
        eventRepository.save(event);
        saveEndpointHit(request);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).toList();
        }
        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }
        return compilations.stream().map(CompilationMapper::toCompilationDto).collect(toList());
    }

    @Override
    public CompilationDto getCompilation(Long comId) {
        Compilation compilation = compilationRepository.findById(comId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Transactional
    private void saveEndpointHit(HttpServletRequest request) {
        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statClient.create(endpointHit);
    }

    private Map<Long, Long> getViewsFromStatService(List<Event> events) {
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
            List<ViewStats> viewStats = Arrays.asList(objectMapper.readValue(
                    objectMapper.writeValueAsString(response.getBody()), ViewStats[].class));
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

    private Map<Long, Long> getConfirmedRequestsFromStatService(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(toList());
        List<EventConfirmedRequests> eventConfirmedRequests =
                requestRepository.getCountOfConfirmedRequestsByEventId(eventIds);
        return eventConfirmedRequests.stream()
                .collect(toMap(EventConfirmedRequests::getEvent, EventConfirmedRequests::getConfirmedRequestsCount));
    }
}