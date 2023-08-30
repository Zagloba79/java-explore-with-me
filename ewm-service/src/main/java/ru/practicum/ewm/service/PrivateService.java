package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PrivateService {
    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size, HttpServletRequest request);

    EventFullDto getEventById(Long userId, Long eventId, HttpServletRequest request);

    List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId);

    EventFullDto createEvent(Long userId, NewEventDto eventDto);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getRequestsByUser(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto canselRequest(Long userId, Long requestsId);
}
