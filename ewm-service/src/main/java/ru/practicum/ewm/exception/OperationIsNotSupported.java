package ru.practicum.ewm.exception;

public class OperationIsNotSupported extends RuntimeException {
    public OperationIsNotSupported(String message) {
        super(message);
    }

    public OperationIsNotSupported(String message, Throwable cause) {
        super(message, cause);
    }
}
