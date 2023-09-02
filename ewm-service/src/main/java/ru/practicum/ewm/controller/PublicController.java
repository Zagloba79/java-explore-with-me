package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.service.PublicService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class PublicController {
    public final PublicService service;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories(@RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(service.getAllCategories(from, size), HttpStatus.OK);
    }

    @GetMapping("/categories/{catId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long catId) {
        return new ResponseEntity<>(service.getCategory(catId), HttpStatus.OK);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventShortDto>> getAllEvents(@RequestParam(required = false) String text,
                                                            @RequestParam(required = false) List<Long> categories,
                                                            @RequestParam(required = false) Boolean paid,
                                                            @RequestParam(required = false) String rangeStart,
                                                            @RequestParam(required = false) String rangeEnd,
                                                            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                            @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                            @RequestParam(defaultValue = "10") @Positive int size,
                                                            HttpServletRequest request) {
        return new ResponseEntity<>(service.getAllEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, request), HttpStatus.OK);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long id, HttpServletRequest request) {
        return new ResponseEntity<>(service.getEvent(id, request), HttpStatus.OK);
    }

    @GetMapping("/compilations")
    public ResponseEntity<List<CompilationDto>> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(service.getAllCompilations(pinned, from, size), HttpStatus.OK);
    }

    @GetMapping("/compilations/{comId}")
    public ResponseEntity<CompilationDto> getCompilation(@PathVariable Long comId) {
        return new ResponseEntity<>(service.getCompilation(comId), HttpStatus.OK);
    }
}
