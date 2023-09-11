package ru.practicum.ewm.dto;

import lombok.*;
import ru.practicum.ewm.enums.Status;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipationRequestDto {
    private Long id;
    private String created;
    private Long event;
    private Long requester;
    private Status status;
}
