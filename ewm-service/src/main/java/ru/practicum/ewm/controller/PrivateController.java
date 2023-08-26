package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.PrivateService;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateController {
    public final PrivateService service;

    @GetMapping("/events")
    public Set<EventShortDto> getAllEvents(@PathVariable Long userId,
                                           @RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return service.getAllEvents(userId, from, size);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return service.getEvent(userId, eventId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return service.getRequestsByEvent(userId, eventId);
    }

    @PostMapping("/events")
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
    public ParticipationRequestDto createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestsId}/cancel")
    public ParticipationRequestDto canselRequest(@PathVariable Long userId, @PathVariable Long requestsId) {
        return service.canselRequest(userId, requestsId);
    }
}