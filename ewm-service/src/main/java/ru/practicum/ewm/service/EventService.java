package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.enums.State;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {
    EventFullDto updateEventAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> getAllEventsAdmin(List<Long> users, List<State> states, List<Long> categories,
                                         LocalDateTime periodStart, LocalDateTime periodEnd, int from, int size);

    List<EventShortDto> getEventsByUserPrivate(Long userId, Integer from, Integer size);

    EventFullDto getEventByIdPrivate(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsByEventPrivate(Long userId, Long eventId);

    EventFullDto createEventPrivate(Long userId, NewEventDto eventDto);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest eventDto);

    List<EventShortDto> getAllEventsPublic(String text, List<Long> categories, Boolean paid, String rangeStart,
                                           String rangeEnd, Boolean onlyAvailable, String sort,
                                           int from, int size, HttpServletRequest request);

    EventFullDto getEventPublic(Long id, HttpServletRequest request);

    Map<Long, Long> getViewsFromStat(List<Event> events);
}
