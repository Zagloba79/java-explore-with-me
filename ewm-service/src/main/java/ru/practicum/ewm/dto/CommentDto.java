package ru.practicum.ewm.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private long id;
    private String text;
    private UserDto author;
    private EventCommentDto event;
    private String createTime;
    private String lastUpdateTime;
}
