package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.entity.Location;
import ru.practicum.ewm.enums.StateActionForAdmin;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest {
    @Min(20)
    @Max(2000)
    private String annotation;
    private Long categoryId;
    @Min(20)
    @Max(7000)
    private String description;
    private String eventDate;
    private Location location;
    private boolean paid;
    private Integer participantLimit;
    private boolean requestModeration;
    private StateActionForAdmin stateAction;
    @Min(3)
    @Max(120)
    private String title;
}
