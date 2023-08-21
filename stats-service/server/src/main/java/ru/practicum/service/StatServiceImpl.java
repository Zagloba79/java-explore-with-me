package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.ViewStats;
import ru.practicum.mapper.ViewStatsMapper;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {
    private final StatRepository repository;

    @Override
    @Transactional
    public EndpointHitDto create(EndpointHitDto endpointHitDto) {
        return EndpointHitMapper.createEndpointHitDto(
                repository.save(EndpointHitMapper.createEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStatsDto> getStatsList(List<String> uris, LocalDateTime start, LocalDateTime end, Boolean unique) {
        List<ViewStats> viewStats;
        if (uris == null) {
            if (unique) {
                viewStats = repository.getUniqueHitsList(start, end);
            } else {
                viewStats = repository.getHitsList(start, end);
            }
        }
        if (unique) {
            viewStats = repository.getUniqueHitsList(uris, start, end);
        } else {
            viewStats = repository.getHitsList(uris, start, end);
        }
        return viewStats.stream()
                .map(ViewStatsMapper::createViewStatsDto)
                .collect(toList());
    }
}