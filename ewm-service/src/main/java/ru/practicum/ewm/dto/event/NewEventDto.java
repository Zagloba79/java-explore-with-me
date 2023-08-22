package ru.practicum.ewm.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.entity.Location;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @Min(20)
    @Max(2000)
    private String annotation;
    private Long categoryId;
    @Min(20)
    @Max(7000)
    private String description;
    private String eventDate;
    private Location location;
    private boolean paid = false;
    private Integer participantLimit = 0;
    private boolean requestModeration = true;
    @Min(3)
    @Max(120)
    private String title;
}
