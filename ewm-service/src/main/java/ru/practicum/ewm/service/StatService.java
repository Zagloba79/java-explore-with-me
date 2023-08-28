package ru.practicum.ewm.service;

import ru.practicum.ewm.entity.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface StatService {
    Map<Long, Long> toEventConfirmedRequests(List<Event> events);

    Map<Long, Long> toView(List<Event> events);

    void saveEndpointHit(HttpServletRequest request, String serviceName);
}