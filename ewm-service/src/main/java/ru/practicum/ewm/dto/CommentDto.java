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
    private EventShortDto eventDto;
    private String createTime;
    private String lastUpdateTime;
}
