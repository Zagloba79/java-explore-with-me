package ru.practicum.ewm.dto;

import lombok.*;
import ru.practicum.ewm.entity.Location;
import ru.practicum.ewm.enums.State;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto categoryDto;
    private Long confirmedRequests;
    private String createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private Location location;
    private boolean paid;
    private Integer participantLimit = 0;
    private String publishedOn;
    private boolean requestModeration = true;
    private State state;
    private String title;
    private Long views;
}