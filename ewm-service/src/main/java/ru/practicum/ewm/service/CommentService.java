package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.CommentShortDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    List<CommentDto> findCommentsByTextAdmin(String text, int from, int size);

    void deleteCommentAdmin(Long commentId);

    CommentDto createCommentPrivate(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateCommentPrivate(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    void deleteCommentPrivate(Long userId, Long commentId);

    CommentDto getCommentPublic(Long commentId);

    List<CommentShortDto> getCommentsByEventPublic(Long eventId, int from, int size);
}
