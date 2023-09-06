package ru.practicum.ewm.exception;

public class StatException extends RuntimeException {

    public StatException(String s) {
        super(s);
    }

    public StatException(String s, Exception e) {
        super(s, e);
    }
}
