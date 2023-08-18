package ru.practicum.mapper;

import ru.practicum.ViewStatsDto;
import ru.practicum.model.ViewStats;

public class ViewStatsMapper {
    public static ViewStats createViewStats(ViewStatsDto viewStatsDto) {
        ViewStats viewStats = new ViewStats();
        viewStats.setApp(viewStatsDto.getApp());
        viewStats.setUri(viewStatsDto.getUri());
        viewStats.setHits(viewStatsDto.getHits());
        return viewStats;
    }

    public static ViewStatsDto createViewStatsDto(ViewStats viewStats) {
        ViewStatsDto viewStatsDto = new ViewStatsDto();
        viewStatsDto.setApp(viewStats.getApp());
        viewStatsDto.setUri(viewStats.getUri());
        viewStatsDto.setHits(viewStats.getHits());
        return viewStatsDto;
    }
}
