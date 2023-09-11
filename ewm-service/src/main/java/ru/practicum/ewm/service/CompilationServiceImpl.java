package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.entity.Compilation;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.util.InfoFromRep;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final InfoFromRep infoFromRep;

    @Override
    public CompilationDto createCompilationAdmin(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        return mapToCompilationDto(compilation, newCompilationDto.getEvents());
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationAdmin(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        return mapToCompilationDto(compilation, updateCompilationRequest.getEvents());
    }

    @Override
    @Transactional
    public void deleteCompilationAdmin(long compId) {
        boolean isExist = compilationRepository.existsById(compId);
        if (!isExist) {
            throw new ObjectNotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getAllCompilationsPublic(Boolean pinned, int from, int size) {
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
        List<CompilationDto> compilationsDto = new ArrayList<>();
        for (Compilation compilation : compilations) {
            List<Long> eventIds = compilation.getEvents().stream().map(Event::getId).collect(toList());
            compilationsDto.add(mapToCompilationDto(compilation, eventIds));
        }
        return compilationsDto;
    }

    @Override
    public CompilationDto getCompilationPublic(Long comId) {
        Compilation compilation = compilationRepository.findById(comId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        List<Long> eventIds = compilation.getEvents().stream().map(Event::getId).collect(toList());
        return mapToCompilationDto(compilation, eventIds);
    }

    private CompilationDto mapToCompilationDto(Compilation compilation, List<Long> eventIds) {
        Set<Event> events = findEvents(eventIds);
        Map<Long, Long> viewsFromRep;
        if (!events.isEmpty()) {
            compilation.setEvents(events);
            viewsFromRep = infoFromRep.getViewsFromStat(new ArrayList<>(events));
        } else {
            viewsFromRep = Collections.emptyMap();
        }
        compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation, viewsFromRep);
    }

    private Set<Event> findEvents(List<Long> eventIds) {
        if (eventIds == null) {
            return Collections.emptySet();
        }
        return eventRepository.findAllByIdIn(eventIds);
    }
}