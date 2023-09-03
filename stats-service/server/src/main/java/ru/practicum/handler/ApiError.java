package ru.practicum.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class ApiError {
    private List<String> errors;
    private String message;
    private String reason;
    private String status;
    private String timestamp;
}