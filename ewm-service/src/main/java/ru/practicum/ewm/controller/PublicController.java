package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.PublicService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping
public class PublicController {
    public final PublicService service;

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getAllCategories(@RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        return service.getAllCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable Long catId) {
        return service.getCategory(catId);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getAllEvents(@RequestBody @Valid GetAllEventsParamsDto paramsDto,
                                            HttpServletRequest request) {
        return service.getAllEvents(paramsDto.getText(), paramsDto.getCategories(),
                paramsDto.getPaid(), paramsDto.getRangeStart(), paramsDto.getRangeEnd(), paramsDto.getOnlyAvailable(),
                paramsDto.getSort(), paramsDto.getFrom(), paramsDto.getSize(), request);
    }

    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        return service.getEvent(id, request);
    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size) {
        return service.getAllCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{comId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilation(@PathVariable Long comId) {
        return service.getCompilation(comId);
    }
}
