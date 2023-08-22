package ru.practicum.ewm.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.entity.Location;
import ru.practicum.ewm.enums.State;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
