package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.AdminService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin")
public class AdminController {
    private final AdminService service;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        return service.createUser(newUserRequest);
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers(@RequestParam(required = false) List<Long> ids,
                                     @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                     @RequestParam(defaultValue = "10") @Positive Integer size) {
        return service.getAllUsers(ids, from, size);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        service.deleteUser(userId);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return service.createCategory(newCategoryDto);
    }

    @PatchMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable("catId") Long catId,
                                      @RequestBody @Valid CategoryDto categoryDto) {
        return service.updateCategory(catId, categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        service.deleteCategory(catId);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        return service.createCompilation(newCompilationDto);
    }

    @PatchMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @RequestBody
                                            @Valid UpdateCompilationRequest updateCompilationRequest) {
        return service.updateCompilation(compId, updateCompilationRequest);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        service.deleteCompilation(compId);
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @RequestBody
                                    @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        return service.updateEvent(eventId, updateEventAdminRequest);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEvents(@RequestBody @Valid EventParamsDto eventParamsDto) {
        return service.getAllEvents(eventParamsDto.getUsers(), eventParamsDto.getStates(),
                eventParamsDto.getCategories(), eventParamsDto.getRangeStart(), eventParamsDto.getRangeEnd(),
                eventParamsDto.getFrom(), eventParamsDto.getSize());
    }
}