package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<EventShortDto>> getEventsByUser(@PathVariable Long userId,
                                                               @RequestParam(defaultValue = "0") Integer from,
                                                               @RequestParam(defaultValue = "10") Integer size,
                                                               HttpServletRequest request) {
        return new ResponseEntity<>(service.getEventsByUser(userId, from, size, request), HttpStatus.OK);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long userId,
                                                 @PathVariable Long eventId,
                                                 HttpServletRequest request) {
        return new ResponseEntity<>(service.getEventById(userId, eventId, request), HttpStatus.OK);
    }

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsByEvent(@PathVariable Long userId,
                                                                            @PathVariable Long eventId) {
        return new ResponseEntity<>(service.getRequestsByEvent(userId, eventId), HttpStatus.OK);
    }

    @PostMapping("/events")
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Long userId,
                                                    @RequestBody @Valid NewEventDto eventDto) {
        return new ResponseEntity<>(service.createEvent(userId, eventDto), HttpStatus.CREATED);
    }

    @PatchMapping("/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @RequestBody @Valid UpdateEventUserRequest eventDto) {
        return new ResponseEntity<>(service.updateEvent(userId, eventId, eventDto), HttpStatus.OK);
    }

    @PatchMapping("/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequestStatus(@PathVariable Long userId,
                                                                              @PathVariable Long eventId,
                                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        return new ResponseEntity<>(service.updateRequestStatus(userId, eventId, request), HttpStatus.OK);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsByUser(@PathVariable Long userId) {
        return new ResponseEntity<>(service.getRequestsByUser(userId), HttpStatus.OK);
    }

    @PostMapping("/requests")
    public ResponseEntity<ParticipationRequestDto> createRequest(@PathVariable Long userId,
                                                                 @RequestParam Long eventId) {
        return new ResponseEntity<>(service.createRequest(userId, eventId), HttpStatus.CREATED);
    }

    @PatchMapping("/requests/{requestsId}/cancel")
    public ResponseEntity<ParticipationRequestDto> canselRequest(@PathVariable Long userId,
                                                                 @PathVariable Long requestsId) {
        return new ResponseEntity<>(service.canselRequest(userId, requestsId), HttpStatus.OK);
    }
}