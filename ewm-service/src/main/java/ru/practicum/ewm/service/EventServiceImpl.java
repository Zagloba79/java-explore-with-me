package ru.practicum.ewm.service;

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
import ru.practicum.ViewStatsDto;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.entity.Request;
import ru.practicum.ewm.entity.User;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.enums.StateActionForAdmin;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static ru.practicum.ewm.enums.State.PENDING;
import static ru.practicum.ewm.enums.State.PUBLISHED;
import static ru.practicum.ewm.enums.StateActionForUser.CANCEL_REVIEW;
import static ru.practicum.ewm.enums.StateActionForUser.SEND_TO_REVIEW;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;
    private final ObjectMapper objectMapper;
    private final String app = "ewm_service";


    @Override
    @Transactional
    public EventFullDto updateEventAdmin(long eventId, UpdateEventAdminRequest eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new OperationIsNotSupportedException("Поздняк метаться");
        }
        if (eventDto.getAnnotation() != null && !eventDto.getTitle().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new ObjectNotFoundException("Category not found")));
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (LocalDateTime.parse(eventDto.getEventDate(), FORMATTER).isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Поздняк метаться");
            } else {
                event.setEventDate(LocalDateTime.parse(eventDto.getEventDate(), FORMATTER));
            }
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(eventDto.getLocation());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getStateAction() != null) {
            if (!event.getState().equals(PENDING)) {
                throw new DataIsNotCorrectException("Можно изменять события только в статусе 'Pending'");
            }
            if (eventDto.getStateAction().equals(StateActionForAdmin.PUBLISH_EVENT)) {
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (eventDto.getStateAction().equals(StateActionForAdmin.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            }
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        Long viewsFromRep = getViewsFromStat(List.of(event)).get(event.getId());
        return EventMapper.toEventFullDto(event, viewsFromRep);
    }

//    @Override
//    public List<EventFullDto> getAllEventsAdmin(List<Long> users, List<State> states, List<Long> categories,
//                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
//        if (rangeStart == null) {
//            rangeStart = LocalDateTime.now();
//        }
//        if (rangeEnd == null) {
//            rangeEnd = LocalDateTime.now().plusYears(10);
//        }
//        if (rangeStart.isAfter(rangeEnd)) {
//            throw new ValidationException("Даты попутаны");
//        }
//        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id").ascending());
//        List<Event> events = eventRepository.findAllByParam(users, states, categories, rangeStart, rangeEnd, pageable);
//        if (events.isEmpty()) {
//            return Collections.emptyList();
//        }
//        Map<Long, Long> viewsFromRep = getViewsFromStat(events);
//        return events.stream()
//                .map(event -> EventMapper.toEventFullDto(event, viewsFromRep.get(event.getId())))
//                .collect(toList());
//    }

    @Override
    public List<EventFullDto> getAllEventsAdmin(EventParams eventParams) {
        LocalDateTime rangeStart = eventParams.getRangeStart();
        LocalDateTime rangeEnd = eventParams.getRangeEnd();
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(10);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Даты попутаны");
        }
        Pageable pageable = PageRequest.of(eventParams.getFrom() > 0 ?
                eventParams.getFrom() / eventParams.getSize() : 0, eventParams.getSize(),
                Sort.by("id").ascending());
        List<Event> events = eventRepository.findAllByParam(eventParams.getUsers(),
                eventParams.getStates(), eventParams.getCategories(), rangeStart, rangeEnd, pageable);
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Long> viewsFromRep = getViewsFromStat(events);
        return events.stream()
                .map(event -> EventMapper.toEventFullDto(event, viewsFromRep.get(event.getId())))
                .collect(toList());
    }


    @Override
    @Transactional
    public EventFullDto createEventPrivate(Long userId, NewEventDto eventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        dateValidate(LocalDateTime.parse(eventDto.getEventDate(), FORMATTER));
        Event event = EventMapper.toEvent(eventDto, user);
        event.setCategory(categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new ObjectNotFoundException("Category not found")));
        return EventMapper.toEventFullDto(eventRepository.save(event), 0L);
    }

    @Override
    @Transactional
    public List<EventShortDto> getEventsByUserPrivate(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        List<Event> events = eventRepository.findAllByInitiator_Id(userId, pageable);
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Long> viewsFromRep = getViewsFromStat(events);
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, viewsFromRep.get(event.getId())))
                .collect(toList());
    }

    @Override
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        Map<Long, Long> viewsFromRep = getViewsFromStat(List.of(event));
        return EventMapper.toEventFullDto(event, viewsFromRep.get(event.getId()));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventPrivate(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new ObjectNotFoundException("Event not found");
        }
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (event.getState().equals(PUBLISHED)) {
            throw new DataIsNotCorrectException("Нельзя менять опубликованные события");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Поздняк метаться");
        }
        if (eventDto.getAnnotation() != null && !eventDto.getTitle().isBlank()) {
            if (eventDto.getAnnotation().length() >= 20 && eventDto.getAnnotation().length() <= 2000) {
                event.setAnnotation(eventDto.getAnnotation());
            }
        }
        if (eventDto.getCategoryId() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategoryId())
                    .orElseThrow(() -> new ObjectNotFoundException("Category not found")));
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            if (eventDto.getDescription().length() >= 20 && eventDto.getDescription().length() <= 7000) {
                event.setDescription(eventDto.getDescription());
            }
        }
        if (eventDto.getEventDate() != null) {
            if (LocalDateTime.parse(eventDto.getEventDate(), FORMATTER).isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Поздняк метаться");
            } else {
                event.setEventDate(LocalDateTime.parse(eventDto.getEventDate(), FORMATTER));
            }
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(eventDto.getLocation());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(SEND_TO_REVIEW)) {
                event.setState(State.PENDING);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (eventDto.getStateAction().equals(CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
            }
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            if (eventDto.getTitle().length() >= 3 && eventDto.getTitle().length() <= 120) {
                event.setTitle(eventDto.getTitle());
            }
        }
        Map<Long, Long> viewsFromRep = getViewsFromStat(List.of(event));
        return EventMapper.toEventFullDto(event, viewsFromRep.get(event.getId()));
    }

    @Override
    @Transactional
    public List<EventShortDto> getAllEventsPublic(String text, List<Long> categories, Boolean paid,
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
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by(sort).descending());
        List<Event> events = eventRepository.getAllByParam(text, categories, paid, startTime,
                endTime, onlyAvailable, pageable);
        Map<Long, Long> views = getViewsFromStat(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequestsFromStat(events);
        List<EventShortDto> eventShorts = new ArrayList<>();
        for (Event event : events) {
            event.setConfirmedRequests(confirmedRequests.get(event.getId()));
            Long viewsFromRep = views.get(event.getId());
            eventShorts.add(EventMapper.toEventShortDto(event, viewsFromRep));
            saveEndpointHit("/events/" + event.getId(), request.getRemoteAddr());
        }
        eventRepository.saveAll(events);
        saveEndpointHit(request);
        return eventShorts;
    }

    @Override
    @Transactional
    public EventFullDto getEventPublic(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (!event.getState().equals(PUBLISHED)) {
            throw new ObjectNotFoundException("Event is not published");
        }
        Map<Long, Long> views = getViewsFromStat(List.of(event));
        Map<Long, Long> confirmedRequests = getConfirmedRequestsFromStat(List.of(event));
        event.setConfirmedRequests(confirmedRequests.get(event.getId()));
        eventRepository.save(event);
        saveEndpointHit(request);
        Long viewsFromRep = views.get(event.getId());
        return EventMapper.toEventFullDto(event, viewsFromRep);
    }

    public void saveEndpointHit(HttpServletRequest request) {
        saveEndpointHit(request.getRequestURI(), request.getRemoteAddr());
    }

    public void saveEndpointHit(String uri, String remoteAddr) {
        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(uri)
                .ip(remoteAddr)
                .timestamp(LocalDateTime.now())
                .build();
        statClient.create(endpointHit);
    }

    private void dateValidate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Время должно быть больше на 2 часа чем сейчас");
        }
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

    public Map<Long, Long> getConfirmedRequestsFromStat(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(toList());
        List<EventConfirmedRequests> eventConfirmedRequests =
                requestRepository.getCountOfConfirmedRequestsByEventId(eventIds);
        return eventConfirmedRequests.stream()
                .collect(toMap(EventConfirmedRequests::getEvent, EventConfirmedRequests::getConfirmedRequestsCount));
    }
}