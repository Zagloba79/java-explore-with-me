package ru.practicum.ewm.service;

import ru.practicum.ewm.entity.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface EwmStatService {
    void saveEndpointHit(HttpServletRequest request);

    Map<Long, Long> getViewsFromStat(List<Event> events);

    Map<Long, Long> getConfirmedRequestsFromStat(List<Event> events);

    void saveEndpointHit(String uri, String remoteAddr);
}
