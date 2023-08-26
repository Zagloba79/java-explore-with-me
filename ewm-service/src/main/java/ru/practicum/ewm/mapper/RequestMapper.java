package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.entity.Request;
import ru.practicum.ewm.entity.User;

import java.time.LocalDateTime;

import static ru.practicum.ewm.enums.Status.*;

public class RequestMapper {
    public static Request toRequest(Event event, User requester) {
        return Request.builder()
                .requester(requester)
                .event(event)
                .created(LocalDateTime.now())
                .status(event.getRequestModeration() ? PENDING : CONFIRMED)
                .build();
    }

    public static ParticipationRequestDto toParticipationRequestDto(Request entity) {
        return ParticipationRequestDto.builder()
                .id(entity.getId())
                .created(entity.getCreated())
                .requesterId(entity.getRequester().getId())
                .eventId(entity.getEvent().getId())
                .status(entity.getStatus())
                .build();
    }
}
