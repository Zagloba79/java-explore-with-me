package ru.practicum;

import lombok.experimental.UtilityClass;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class UrlEncodeUtils {
    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}