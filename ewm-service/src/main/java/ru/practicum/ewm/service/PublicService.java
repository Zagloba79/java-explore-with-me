package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface PublicService {
    List<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategory(Long catId);

    Set<EventShortDto> getAllEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                              LocalDateTime rangeEnd, Boolean onlyAvailable, String sort,
                              int from, int size, HttpServletRequest request);
    EventFullDto getEvent(Long id, HttpServletRequest request);

    List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long comId);
}
