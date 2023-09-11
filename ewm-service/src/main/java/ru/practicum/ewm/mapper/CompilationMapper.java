package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.entity.Compilation;
import ru.practicum.ewm.entity.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class CompilationMapper {

    public Compilation toCompilation(NewCompilationDto newCompilationDto) {
        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.isPinned())
                .events(new HashSet<>())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation, Map<Long, Long> viewsFromRep) {
        CompilationDto compilationDto = CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
        Set<Event> events = compilation.getEvents();
        if (events != null) {
            List<EventShortDto> eventsShortDto = events.stream()
                    .map(event -> EventMapper.toEventShortDto(event, viewsFromRep.get(event.getId())))
                    .collect(toList());
            compilationDto.setEvents(eventsShortDto);
        }
        return compilationDto;
    }
}