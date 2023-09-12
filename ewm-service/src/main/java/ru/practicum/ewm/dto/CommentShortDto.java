package ru.practicum.ewm.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentShortDto {
    private long id;
    private String text;
    private UserDto author;
    private EventCommentDto eventDto;
    private String lastUpdateTime;
}
