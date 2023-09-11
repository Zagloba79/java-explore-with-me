package ru.practicum.ewm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UpdateCommentDto {
    @Size(min = 1, max = 1000)
    private String text;
    @NotNull
    private Long authorId;
    @NotNull
    private Long eventId;
    @NotBlank
    private String updateTime;
}
