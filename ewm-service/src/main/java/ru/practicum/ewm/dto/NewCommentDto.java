package ru.practicum.ewm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class NewCommentDto {
    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;
}
