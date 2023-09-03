package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatClient;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entity.*;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.enums.Status;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static ru.practicum.ewm.enums.State.*;
import static ru.practicum.ewm.enums.StateActionForUser.*;
import static ru.practicum.ewm.enums.Status.REJECTED;
import static ru.practicum.ewm.enums.Status.CANCELED;
import static ru.practicum.ewm.enums.Status.CONFIRMED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateServiceImpl implements PrivateService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final StatClient statClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        dateValidate(LocalDateTime.parse(eventDto.getEventDate(), FORMATTER));
        Event event = EventMapper.toEvent(eventDto);
        event.setCategory(categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new ObjectNotFoundException("Category not found")));
        event.setPublishedOn(LocalDateTime.now());
        event.setConfirmedRequests(0L);
        event.setInitiator(user);
        event.setViews(0L);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        List<Event> events = eventRepository.findAllByInitiator_Id(userId, pageable);
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        for (Event event : events) {
            saveEndpointHit(request);
            event.setViews(event.getViews() + 1);
        }
        return events.stream().map(EventMapper::toEventShortDto).collect(toList());

    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId, HttpServletRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        saveEndpointHit(request);
        event.setViews(event.getViews() + 1);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
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
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
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
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() != null &&
                event.getConfirmedRequests() == event.getParticipantLimit()) {
            throw new DataIsNotCorrectException("Лимит заявок на участие исчерпан");
        }
        Optional<Request> requestFromRep = requestRepository.findByEventIdAndRequesterId(eventId, userId);
        if (requestFromRep.isPresent()) {
            throw new DataIsNotCorrectException("Запрос от пользователя на это событие уже есть");
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new DataIsNotCorrectException("Initiator can't be a requester");
        }
        if (!event.getState().equals(PUBLISHED)) {
            throw new DataIsNotCorrectException("Event is not published");
        }
        Request request;
        if (event.getParticipantLimit() == 0) {
            request = new Request(LocalDateTime.now(), event, user, Status.CONFIRMED);
            Long confirmedRequests = Optional.ofNullable(event.getConfirmedRequests()).orElse(0L);
            event.setConfirmedRequests(confirmedRequests + 1);
            eventRepository.save(event);
        } else {
            request = new Request(LocalDateTime.now(), event, user, Status.PENDING);
            if (event.getRequestModeration().equals(false)) {
                request.setStatus(Status.CONFIRMED);
                Long confirmedRequests = Optional.ofNullable(event.getConfirmedRequests()).orElse(0L);
                event.setConfirmedRequests(confirmedRequests + 1);
                eventRepository.save(event);
            }
        }
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest statusUpdateRequests) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new OperationIsNotSupportedException("Вы не являетесь инициатором события");
        }
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() != null &&
                event.getConfirmedRequests() == event.getParticipantLimit()) {
            throw new DataIsNotCorrectException("Лимит заявок на участие исчерпан");
        }
        EventRequestStatusUpdateResult updatedRequests = new EventRequestStatusUpdateResult();
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        List<Request> requestsForUpdate = requests.stream().filter(r -> statusUpdateRequests.getRequestIds()
                .contains(r.getId())).collect(toList());
        for (Request request : requestsForUpdate) {
            if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() != null &&
                    (event.getConfirmedRequests() == event.getParticipantLimit())) {
                request.setStatus(REJECTED);
            }
            if (request.getStatus().equals(Status.PENDING)) {
                if (statusUpdateRequests.getStatus().equals(CONFIRMED)) {
                    request.setStatus(CONFIRMED);
                    Long confirmedRequests = Optional.ofNullable(event.getConfirmedRequests()).orElse(0L);
                    event.setConfirmedRequests(confirmedRequests + 1);
                    List<ParticipationRequestDto> confirmedRequestsList = updatedRequests.getConfirmedRequests();
                    confirmedRequestsList.add(RequestMapper.toParticipationRequestDto(request));
                    updatedRequests.setConfirmedRequests(confirmedRequestsList);
                }
                if ((statusUpdateRequests.getStatus().equals(REJECTED))) {
                    request.setStatus(REJECTED);
                    List<ParticipationRequestDto> rejectedRequests = updatedRequests.getRejectedRequests();
                    rejectedRequests.add(RequestMapper.toParticipationRequestDto(request));
                    updatedRequests.setRejectedRequests(rejectedRequests);
                }
            }
        }
        return updatedRequests;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto canselRequest(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Request not found"));
        request.setStatus(CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    private void dateValidate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Время должно быть больше на 2 часа чем сейчас");
        }
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
}