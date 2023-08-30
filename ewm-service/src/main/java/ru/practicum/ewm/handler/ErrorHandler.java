package ru.practicum.ewm.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.*;

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

    private ApiError buildApiError(Exception ex, HttpStatus status) {
        List<String> errors = Collections.singletonList(traceToString(ex));
        String message = ex.getMessage();
        String reason = status.getReasonPhrase();
        String statusCode = status.name();
        return buildApiError(errors, message, reason, statusCode);
    }

    private String traceToString(final Exception ex) {
        final StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }


    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleObjectNotFoundException(final ObjectNotFoundException e) {
        return buildApiError(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OperationIsNotSupportedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleOperationIsNotSupportedException(final OperationIsNotSupportedException e) {
        return buildApiError(e, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        return buildApiError(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleObjectAlreadyExistsException(final ObjectAlreadyExistsException e) {
        return buildApiError(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ObjectContainsDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleObjectContainsDataException(final ObjectContainsDataException e) {
        return buildApiError(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIsNotCorrectException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIsNotCorrectException(final DataIsNotCorrectException e) {
        return buildApiError(e, HttpStatus.CONFLICT);
    }
}