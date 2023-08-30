package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.PrivateService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateController {
    public final PrivateService service;

    @GetMapping("/events")
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               HttpServletRequest request) {
        return service.getEventsByUser(userId, from, size, request);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId, @PathVariable Long eventId, HttpServletRequest request) {
        return service.getEventById(userId, eventId, request);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return service.getRequestsByEvent(userId, eventId);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto eventDto) {
        return service.createEvent(userId, eventDto);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest eventDto) {
        return service.updateEvent(userId, eventId, eventDto);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        return service.updateRequestStatus(userId, eventId, request);
    }

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getRequestsByUser(@PathVariable Long userId) {
        return service.getRequestsByUser(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestsId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ParticipationRequestDto canselRequest(@PathVariable Long userId, @PathVariable Long requestsId) {
        return service.canselRequest(userId, requestsId);
    }
}