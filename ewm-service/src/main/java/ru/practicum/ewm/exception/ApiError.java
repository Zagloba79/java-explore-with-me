package ru.practicum.ewm.exception;

import lombok.Getter;
import lombok.Setter;

import ru.practicum.ewm.mapper.DateMapper;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiError {
    private String status;
    private String reason;
    private String message;
    private String timestamp;

    public ApiError(String status, String reason, String message) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.timestamp = DateMapper.toStringFormat(LocalDateTime.now());
    }
}