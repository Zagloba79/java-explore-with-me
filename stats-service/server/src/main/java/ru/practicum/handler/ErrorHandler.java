package ru.practicum.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.BadRequestException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class ErrorHandler {
    private ApiError buildApiError(List<String> errors, String message, String reason, String status) {
        return ApiError.builder()
                .errors(errors)
                .message(message)
                .reason(reason)
                .status(status)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    private ApiError buildApiError(Exception ex) {
        List<String> errors = Collections.singletonList(traceToString(ex));
        String message = ex.getMessage();
        String reason = HttpStatus.BAD_REQUEST.getReasonPhrase();
        String statusCode = HttpStatus.BAD_REQUEST.name();
        return buildApiError(errors, message, reason, statusCode);
    }

    private String traceToString(final Exception ex) {
        final StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final Exception e) {
        return buildApiError(e);
    }

}