package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.entity.Location;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank
    @Min(20)
    @Max(2000)
    private String annotation;
    @NotNull
    private Long categoryId;
    @NotBlank
    @Min(20)
    @Max(7000)
    private String description;
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    @NotNull
    private LocalDateTime eventDate;
    @Valid
    @NotNull
    private Location location;
    private boolean paid = false;
    @PositiveOrZero
    private Integer participantLimit = 0;
    @NotNull
    private boolean requestModeration = true;
    @NotBlank
    @Min(3)
    @Max(120)
    private String title;
}