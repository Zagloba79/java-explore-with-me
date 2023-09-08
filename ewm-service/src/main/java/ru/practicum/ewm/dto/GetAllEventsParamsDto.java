package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllEventsParamsDto {
    String text;
    List<Long> categories;
    Boolean paid;
    String rangeStart;
    String rangeEnd;
    Boolean onlyAvailable = false;
    String sort = "eventDate";
    @PositiveOrZero
    int from = 0;
    @Positive
    int size = 10;
}