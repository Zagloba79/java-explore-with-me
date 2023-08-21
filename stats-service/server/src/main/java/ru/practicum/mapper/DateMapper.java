package ru.practicum.mapper;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateMapper {
    private final DateTimeFormatter dateMapper = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime toLocalDateTimeFormat(String stringDate) {
        return LocalDateTime.parse(stringDate, dateMapper);
    }

    public static String toStringFormat(LocalDateTime date) {
        return date.format(dateMapper);
    }
}