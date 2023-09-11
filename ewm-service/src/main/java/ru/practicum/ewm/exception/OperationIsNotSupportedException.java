package ru.practicum.ewm.exception;

public class OperationIsNotSupportedException extends RuntimeException {
    public OperationIsNotSupportedException(String message) {
        super(message);
    }

    public OperationIsNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
