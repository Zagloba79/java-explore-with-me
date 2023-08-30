package ru.practicum.ewm.service;

import javax.servlet.http.HttpServletRequest;

public interface StatService {

    void saveEndpointHit(HttpServletRequest request, String serviceName);
}