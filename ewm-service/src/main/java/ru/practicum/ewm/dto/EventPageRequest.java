package ru.practicum.ewm.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class EventPageRequest extends PageRequest {
    private final int from;

    public EventPageRequest(int from, int size, Sort sort) {
        super(from / size, size, sort);
        this.from = from;
    }
}
