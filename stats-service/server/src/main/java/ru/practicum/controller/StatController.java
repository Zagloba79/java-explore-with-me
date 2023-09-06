package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
//import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.service.StatServiceImpl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatServiceImpl service;

//    @GetMapping("/stats")
//    public ResponseEntity<List<ViewStatsDto>> get(@RequestParam
//                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
//                                                  @RequestParam
//                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
//                                                  @RequestParam(required = false) List<String> uris,
//                                                  @RequestParam(defaultValue = "false") Boolean unique) {
//        validateParamForGetMapping(start, end);
//        return new ResponseEntity<>(service.getStatsList(uris, start, end, unique), HttpStatus.OK);
//    }


    @PostMapping("/hit")
    public ResponseEntity<EndpointHitDto> create(@RequestBody EndpointHitDto dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    private void validateParamForGetMapping(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("даты перепутаны");
        }
    }

    public String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> get(@RequestParam String start,
                                                  @RequestParam String end,
                                                  @RequestParam(required = false) List<String> uris,
                                                  @RequestParam(defaultValue = "false") Boolean unique) {
        LocalDateTime startDate = LocalDateTime.parse(decode(start), FORMATTER);
        LocalDateTime endDate = LocalDateTime.parse(decode(end), FORMATTER);
        validateParamForGetMapping(startDate, endDate);
        return new ResponseEntity<>(service.getStatsList(uris, startDate, endDate, unique), HttpStatus.OK);
    }
}