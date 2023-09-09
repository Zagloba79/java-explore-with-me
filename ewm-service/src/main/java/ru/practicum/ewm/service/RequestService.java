package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.*;

import java.util.List;

public interface RequestService {

    EventRequestStatusUpdateResult updateRequestsStatusPrivate(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getRequestsByUserPrivate(Long userId);

    ParticipationRequestDto createRequestPrivate(Long userId, Long eventId);

    ParticipationRequestDto canselRequestPrivate(Long userId, Long requestsId);
}
