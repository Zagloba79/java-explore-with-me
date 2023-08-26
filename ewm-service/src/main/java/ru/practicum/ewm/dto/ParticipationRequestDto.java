package ru.practicum.ewm.dto;

import lombok.*;
import ru.practicum.ewm.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipationRequestDto {
    private Long id;
    private LocalDateTime created;
    private Long eventId;
    private Long requesterId;
    private Status status;
}
