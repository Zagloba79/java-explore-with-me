package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entity.*;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.exception.ObjectAlreadyExistsException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.exception.OperationIsNotSupported;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static ru.practicum.ewm.enums.State.*;
import static ru.practicum.ewm.enums.State.PENDING;
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

    @Override
    public Set<EventShortDto> getAllEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        Set<Event> events = eventRepository.findAll(pageable).toSet();
        return events.stream().map(EventMapper::toEventShortDto).collect(toSet());
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new ObjectNotFoundException("Event not found");
        }
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
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
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found "));
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new OperationIsNotSupported("Поздняк метаться");
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
                throw new OperationIsNotSupported("Поздняк метаться");
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
            if (!event.getState().equals(PENDING)) {
                throw new OperationIsNotSupported("Можно изменять события только в статусе 'Pending'");
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
                                                              EventRequestStatusUpdateRequest statusUpdateRequest) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectAlreadyExistsException("Пользователя с id " + userId + " не существует");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectAlreadyExistsException("События с id " + eventId + " не существует"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new OperationIsNotSupported("Вы не инициатор события");
        }
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new OperationIsNotSupported("Нет свободный заявок на участие");
        }
        EventRequestStatusUpdateResult updatedRequest = new EventRequestStatusUpdateResult();
        statusUpdateRequest.getRequestIds().forEach(requestId -> {
            Request request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new ObjectNotFoundException("Данного запроса не найденно"));
            if (statusUpdateRequest.getStatus().equals(CONFIRMED)) {
                request.setStatus(CONFIRMED);
                updatedRequest.getConfirmedRequests().add(RequestMapper.toParticipationRequestDto(request));
            }
            if ((statusUpdateRequest.getStatus().equals(REJECTED))) {
                request.setStatus(REJECTED);
                updatedRequest.getRejectedRequests().add(RequestMapper.toParticipationRequestDto(request));
            }
        });
        return updatedRequest;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        if (userRepository.existsById(userId)) {
            List<Request> requests = requestRepository.findAllByRequesterId(userId);
            return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(toList());
        } else {
            throw new ObjectNotFoundException("User not found");
        }
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found "));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ObjectAlreadyExistsException(String.format("Request with requesterId=%d and eventId=%d already exist", userId, eventId));
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new OperationIsNotSupported("Initiator can't be a requester");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new OperationIsNotSupported("Event is not published");
        }
        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            throw new OperationIsNotSupported("Limit is reached");
        }
        if (!event.getRequestModeration()) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.toParticipationRequestDto(requestRepository.save(RequestMapper.toRequest(event, user)));
    }

    @Override
    @Transactional
    public ParticipationRequestDto canselRequest(Long userId, Long requestId) {
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
}