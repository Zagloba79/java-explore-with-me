package ru.practicum.ewm.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventCommentDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private String eventDate;
    private UserShortDto initiator;
    private boolean paid;
    private String title;
}
