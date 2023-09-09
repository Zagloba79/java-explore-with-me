package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.entity.Request;
import ru.practicum.ewm.entity.User;
import ru.practicum.ewm.enums.Status;
import ru.practicum.ewm.exception.DataIsNotCorrectException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.exception.OperationIsNotSupportedException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static ru.practicum.ewm.enums.State.PUBLISHED;
import static ru.practicum.ewm.enums.Status.*;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public ParticipationRequestDto createRequestPrivate(Long userId, Long eventId) {
        Optional<Request> requestFromRep = requestRepository.findByEventIdAndRequesterId(eventId, userId);
        if (requestFromRep.isPresent()) {
            throw new DataIsNotCorrectException("Запрос от пользователя на это событие уже есть");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() != null &&
                event.getConfirmedRequests() == event.getParticipantLimit()) {
            throw new DataIsNotCorrectException("Лимит заявок на участие исчерпан");
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
    public EventRequestStatusUpdateResult updateRequestsStatusPrivate(Long userId,
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
    public List<ParticipationRequestDto> getRequestsByUserPrivate(Long userId) {
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
    public ParticipationRequestDto canselRequestPrivate(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Request not found"));
        request.setStatus(CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }
}