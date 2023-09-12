package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.CommentService;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.RequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}")
public class PrivateController {
    public final EventService eventService;
    public final RequestService requestService;
    private final CommentService commentService;

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getEventsByUserPrivate(userId, from, size);
    }

    @GetMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        return eventService.getEventByIdPrivate(userId, eventId);
    }

    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long userId,
                                                            @PathVariable Long eventId) {
        return eventService.getRequestsByEventPrivate(userId, eventId);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @RequestBody @Valid NewEventDto eventDto) {
        return eventService.createEventPrivate(userId, eventDto);
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest eventDto) {
        return eventService.updateEventPrivate(userId, eventId, eventDto);
    }

    @PatchMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        return requestService.updateRequestsStatusPrivate(userId, eventId, request);
    }

    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByUser(@PathVariable Long userId) {
        return requestService.getRequestsByUserPrivate(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        return requestService.createRequestPrivate(userId, eventId);
    }

    @PatchMapping("/requests/{requestsId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto canselRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestsId) {
        return requestService.canselRequestPrivate(userId, requestsId);
    }

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId, @PathVariable Long eventId,
                                    @RequestBody @Validated NewCommentDto newCommentDto) {
        return commentService.createCommentPrivate(userId, eventId, newCommentDto);
    }

    @PatchMapping("/comments/{comId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Long userId, @PathVariable Long commentId,
                                   @RequestBody @Validated UpdateCommentDto updateCommentDto) {
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAllMyComments(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        return commentService.getAllMyCommentsPrivate(userId, from, size);
    }

    @DeleteMapping("/comments/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteCommentPrivate(userId, commentId);
    }
}