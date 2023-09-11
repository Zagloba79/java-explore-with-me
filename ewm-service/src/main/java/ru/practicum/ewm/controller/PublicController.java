package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.CategoryService;
import ru.practicum.ewm.service.CommentService;
import ru.practicum.ewm.service.CompilationService;
import ru.practicum.ewm.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping
public class PublicController {
    public final CategoryService categoryService;
    public final CompilationService compilationService;
    public final EventService eventService;
    private final CommentService commentService;

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getAllCategories(@RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        return categoryService.getAllCategoriesPublic(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable Long catId) {
        return categoryService.getCategoryPublic(catId);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getAllEvents(@Valid GetAllEventsParams paramsDto,
                                            HttpServletRequest request) {
        return eventService.getAllEventsPublic(paramsDto, request);
    }

    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getEventPublic(id, request);
    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size) {
        return compilationService.getAllCompilationsPublic(pinned, from, size);
    }

    @GetMapping("/compilations/{comId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilation(@PathVariable Long comId) {
        return compilationService.getCompilationPublic(comId);
    }

    @GetMapping("/comments/{comId}")
    public CommentDto getComment(@PathVariable Long comId) {
        return commentService.getComment(comId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentShortDto> getCommentsByEvent(@PathVariable Long eventId,
                                                    @RequestParam(defaultValue = "0") int from,
                                                    @RequestParam(defaultValue = "10") int size) {
        return commentService.getCommentsByEvent(eventId, from, size);
    }
}