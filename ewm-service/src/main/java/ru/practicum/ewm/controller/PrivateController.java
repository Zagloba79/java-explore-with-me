package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.PrivateService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}")
public class PrivateController {
    public final PrivateService service;

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return service.getEventsByUser(userId, from, size);
    }

    @GetMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        return service.getEventById(userId, eventId);
    }

    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long userId,
                                                            @PathVariable Long eventId) {
        return service.getRequestsByEvent(userId, eventId);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @RequestBody @Valid NewEventDto eventDto) {
        return service.createEvent(userId, eventDto);
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest eventDto) {
        return service.updateEvent(userId, eventId, eventDto);
    }

    @PatchMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        return service.updateRequestsStatus(userId, eventId, request);
    }

    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByUser(@PathVariable Long userId) {
        return service.getRequestsByUser(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestsId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto canselRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestsId) {
        return service.canselRequest(userId, requestsId);
    }
}