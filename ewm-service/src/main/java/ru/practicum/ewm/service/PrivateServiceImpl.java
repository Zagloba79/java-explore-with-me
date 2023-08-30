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
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.practicum.ewm.enums.State.*;
import static ru.practicum.ewm.enums.StateActionForUser.*;
import static ru.practicum.ewm.enums.Status.*;
import static ru.practicum.ewm.enums.Status.CANCELED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateServiceImpl implements PrivateService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final StatClient statClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        dateValidate(eventDto.getEventDate());
        Event event = EventMapper.toEvent(eventDto);
        event.setCategory(categoryRepository.findById(eventDto.getCategoryId())
                .orElseThrow(() -> new ObjectNotFoundException("Category not found")));
        event.setPublishedOn(LocalDateTime.now());
        event.setInitiator(userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found")));
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
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
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
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataIsNotCorrectException("Поздняк метаться");
        }
        if (eventDto.getAnnotation() != null && !eventDto.getTitle().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategoryId() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategoryId())
                    .orElseThrow(() -> new ObjectNotFoundException("Category not found")));
        }
        if (!eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            event.setEventDate(eventDto.getEventDate());
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DataIsNotCorrectException("Поздняк метаться");
            } else {
                event.setEventDate(eventDto.getEventDate());
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
            if (event.getState().equals(PUBLISHED)) {
                throw new DataIsNotCorrectException("Нельзя менять опубликованные события");
            }
            if (eventDto.getStateAction().equals(SEND_TO_REVIEW)) {
                event.setState(PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (eventDto.getStateAction().equals(CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
            }
        }
        if (!eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        return EventMapper.toEventFullDto(event);
    }


    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest statusUpdateRequests) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() == event.getParticipantLimit()) {
            throw new DataIsNotCorrectException("Лимит заявок на участие исчерпан");
        }
        if (event.getRequestModeration().equals(true) || event.getParticipantLimit() > 0) {
            if (!event.getInitiator().getId().equals(userId)) {
                throw new OperationIsNotSupportedException("Вы не инициатор события");
            }
        }
        EventRequestStatusUpdateResult updatedRequests = new EventRequestStatusUpdateResult();
        statusUpdateRequests.getRequestIds().forEach(requestId -> {
            Request request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new ObjectNotFoundException("Данного запроса не найденно"));
            if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() == event.getParticipantLimit()) {
                request.setStatus(REJECTED);
            }
            if (request.getStatus().equals(Status.PENDING)) {
                if (statusUpdateRequests.getStatus().equals(CONFIRMED)) {
                    request.setStatus(CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    updatedRequests.getConfirmedRequests().add(RequestMapper.toParticipationRequestDto(request));
                }
                if ((statusUpdateRequests.getStatus().equals(REJECTED))) {
                    request.setStatus(REJECTED);
                    updatedRequests.getRejectedRequests().add(RequestMapper.toParticipationRequestDto(request));
                }
            }
        });
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
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found "));
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ObjectAlreadyExistsException("Request already exist");
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new OperationIsNotSupportedException("Initiator can't be a requester");
        }
        if (!event.getState().equals(PUBLISHED)) {
            throw new OperationIsNotSupportedException("Event is not published");
        }
        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            throw new OperationIsNotSupportedException("Limit is reached");
        }
        Request request = new Request(event, user, Status.PENDING);
        if (event.getRequestModeration().equals(false)) {
            request.setStatus(Status.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
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
            throw new DataIsNotCorrectException("Время должно быть больше на 2 часа чем сейчас");
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