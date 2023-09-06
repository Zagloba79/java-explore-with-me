package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.entity.Category;
import ru.practicum.ewm.entity.Compilation;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static ru.practicum.ewm.enums.State.PUBLISHED;

@Service
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService {
    private final EwmStatService ewmStatService;
    private final CategoryRepository categoriesRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        List<Category> categories = categoriesRepository.findAll(pageable).toList();
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream().map(CategoryMapper::toCategoryDto).collect(toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        final Category category = categoriesRepository.findById(catId)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public List<EventShortDto> getAllEvents(String text, List<Long> categories, Boolean paid,
                                            String rangeStart, String rangeEnd,
                                            Boolean onlyAvailable, String sort, int from, int size,
                                            HttpServletRequest request) {
        LocalDateTime startTime;
        LocalDateTime endTime;
        if (rangeStart == null) {
            startTime = LocalDateTime.now();
        } else {
            startTime = LocalDateTime.parse(rangeStart, FORMATTER);
        }
        if (rangeEnd == null) {
            endTime = LocalDateTime.now().plusYears(10);
        } else {
            endTime = LocalDateTime.parse(rangeEnd, FORMATTER);
        }
        if (startTime.isAfter(endTime)) {
            throw new ValidationException("Даты попутаны");
        }
        sort = sort.equalsIgnoreCase("event_date") ? "eventDate" : sort;
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by(sort).descending());
        List<Event> events = eventRepository.getAllByParam(text, categories, paid, startTime,
                endTime, onlyAvailable, pageable);
        Map<Long, Long> views = ewmStatService.getViewsFromStat(events);
        Map<Long, Long> confirmedRequests = ewmStatService.getConfirmedRequestsFromStat(events);
        List<EventShortDto> eventShorts = new ArrayList<>();
        for (Event event : events) {
            event.setViews(views.get(event.getId()));
            event.setConfirmedRequests(confirmedRequests.get(event.getId()));
            eventShorts.add(EventMapper.toEventShortDto(event));
        }
        eventRepository.saveAll(events);
        ewmStatService.saveEndpointHit(request);
        return eventShorts;
    }

    @Override
    @Transactional
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (!event.getState().equals(PUBLISHED)) {
            throw new ObjectNotFoundException("Event is not published");
        }
        Map<Long, Long> views = ewmStatService.getViewsFromStat(List.of(event));
        Map<Long, Long> confirmedRequests = ewmStatService.getConfirmedRequestsFromStat(List.of(event));
        event.setViews(views.get(event.getId()));
        event.setConfirmedRequests(confirmedRequests.get(event.getId()));
        eventRepository.save(event);
        ewmStatService.saveEndpointHit(request);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).toList();
        }
        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }
        return compilations.stream().map(CompilationMapper::toCompilationDto).collect(toList());
    }

    @Override
    public CompilationDto getCompilation(Long comId) {
        Compilation compilation = compilationRepository.findById(comId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        return CompilationMapper.toCompilationDto(compilation);
    }
}