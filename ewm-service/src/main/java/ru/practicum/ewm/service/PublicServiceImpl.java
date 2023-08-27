package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entity.*;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.exception.OperationIsNotSupported;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static ru.practicum.ewm.enums.State.PUBLISHED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicServiceImpl implements PublicService {
    private final CategoryRepository categoriesRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final StatClient statClient;
    @Value("${ewm.service.name}")
    private String serviceName;

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        List<Category> categories = categoriesRepository.findAll(pageable).toList();
        return categories.stream().map(CategoryMapper::toCategoryDto).collect(toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        final Category category = categoriesRepository.findById(catId)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public Set<EventShortDto> getAllEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request) {
        return null;
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (!event.getState().equals(PUBLISHED)) {
            throw new OperationIsNotSupported("Event is not published");
        }
        saveEndpointHit(request);
        event.setViews(event.getViews() + 1);
        eventRepository.flush();
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
        return compilations.stream().map(CompilationMapper::toCompilationDto).collect(toList());
    }

    @Override
    public CompilationDto getCompilation(Long comId) {
        final Compilation compilation = compilationRepository.findById(comId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        return CompilationMapper.toCompilationDto(compilation);
    }

    private void saveEndpointHit(HttpServletRequest request) {
        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app(serviceName)
                .timestamp(LocalDateTime.now())
                .build();
        statClient.create(endpointHit);
    }

    private EventPageRequest createPageable(String sort, int from, int size) {
        EventPageRequest pageable = null;
        if (sort == null || sort.equalsIgnoreCase("EVENT_DATE")) {
            pageable = new EventPageRequest(from, size,
                    Sort.by(Sort.Direction.ASC, "event_date"));
        } else if (sort.equalsIgnoreCase("VIEWS")) {
            pageable = new EventPageRequest(from, size,
                    Sort.by(Sort.Direction.ASC, "views"));
        }
        return pageable;
    }
}
