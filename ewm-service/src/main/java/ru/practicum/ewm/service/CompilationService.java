package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilationAdmin(NewCompilationDto newCompilationDto);

    void deleteCompilationAdmin(long compId);

    CompilationDto updateCompilationAdmin(long compId, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> getAllCompilationsPublic(Boolean pinned, int from, int size);

    CompilationDto getCompilationPublic(Long comId);
}
