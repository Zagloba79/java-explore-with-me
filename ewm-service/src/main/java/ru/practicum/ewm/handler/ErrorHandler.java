package ru.practicum.ewm.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.*;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundException(final ObjectNotFoundException e) {
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler(OperationIsNotSupported.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleObjectNotFoundException(final OperationIsNotSupported e) {
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleObjectAlreadyExistsException(final ObjectAlreadyExistsException e) {
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler(ObjectContainsDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleObjectContainsDataException(final ObjectContainsDataException e) {
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler(DataIsNotCorrectException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIsNotCorrectException(final DataIsNotCorrectException e) {
        return new ErrorResponse(
                e.getMessage()
        );
    }
}