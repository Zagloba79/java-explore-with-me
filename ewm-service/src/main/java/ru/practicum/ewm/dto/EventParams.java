package ru.practicum.ewm.dto;


import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.enums.State;
import javax.validation.constraints.Future;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventParams {
    List<Long> users;
    List<State> states;
    List<Long> categories;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime rangeStart;
    @Future
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime rangeEnd;
    @PositiveOrZero
    int from = 0;
    @Positive
    int size = 10;
}