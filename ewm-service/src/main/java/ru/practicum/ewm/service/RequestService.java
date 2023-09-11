package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    EventRequestStatusUpdateResult updateRequestsStatusPrivate(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getRequestsByUserPrivate(Long userId);

    ParticipationRequestDto createRequestPrivate(Long userId, Long eventId);

    ParticipationRequestDto canselRequestPrivate(Long userId, Long requestsId);
}
