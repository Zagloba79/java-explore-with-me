package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.service.AdminService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin")
public class AdminController {
    private final AdminService service;

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        return new ResponseEntity<>(service.createUser(newUserRequest), HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(@RequestParam(required = false) List<Long> ids,
                                                     @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                     @RequestParam(defaultValue = "10") @Positive Integer size) {
        return new ResponseEntity<>(service.getAllUsers(ids, from, size), HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        service.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return new ResponseEntity<>(service.createCategory(newCategoryDto), HttpStatus.CREATED);
    }

    @PatchMapping("/categories/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable("catId") long catId,
                                                      @RequestBody @Valid CategoryDto categoryDto) {
        return new ResponseEntity<>(service.updateCategory(catId, categoryDto), HttpStatus.OK);
    }

    @DeleteMapping("/categories/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable long catId) {
        service.deleteCategory(catId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/compilations")
    public ResponseEntity<CompilationDto> createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        return new ResponseEntity<>(service.createCompilation(newCompilationDto), HttpStatus.CREATED);
    }

    @PatchMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(@PathVariable long compId,
                                            @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        return new ResponseEntity<>(service.updateCompilation(compId, updateCompilationRequest), HttpStatus.OK);
    }

    @DeleteMapping("/compilations/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable long compId) {
        service.deleteCompilation(compId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        return new ResponseEntity<>(service.updateEvent(eventId, updateEventAdminRequest), HttpStatus.OK);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventFullDto>> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<State> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        return new ResponseEntity<>(service.getAllEvents(users, states, categories, rangeStart, rangeEnd, from, size), HttpStatus.OK);
    }
}