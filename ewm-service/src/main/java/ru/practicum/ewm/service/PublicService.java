package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PublicService {
    List<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategory(Long catId);

    List<EventShortDto> getAllEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                     String rangeEnd, Boolean onlyAvailable, String sort,
                                     int from, int size, HttpServletRequest request);

    EventFullDto getEvent(Long id, HttpServletRequest request);

    List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long comId);
}