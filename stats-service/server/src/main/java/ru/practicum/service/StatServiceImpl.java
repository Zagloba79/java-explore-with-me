package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.mapper.ViewStatsMapper;
import ru.practicum.model.ViewStats;
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
        return EndpointHitMapper.toEndpointHitDto(
                repository.save(EndpointHitMapper.toEndpointHit(endpointHitDto)));
    }

    public List<EndpointHitDto> getAllEndpointHits(EndpointHitDto endpointHitDto) {
        return repository.findAll().stream()
                .map(EndpointHitMapper::toEndpointHitDto)
                .collect(toList());
    }

    @Override
    public List<ViewStatsDto> getStatsList(List<String> uris, LocalDateTime start, LocalDateTime end, Boolean unique) {
        List<ViewStats> viewStats;
        if (unique) {
            viewStats = CollectionUtils.isEmpty(uris) ?
                    repository.getUniqueHitsList(start, end) : repository.getUniqueHitsListByUris(uris, start, end);
        } else {
            viewStats = CollectionUtils.isEmpty(uris) ?
                    repository.getHitsList(start, end) : repository.getHitsListByUris(uris, start, end);
        }
        return viewStats.stream()
                .map(ViewStatsMapper::createViewStatsDto)
                .collect(toList());
    }
}