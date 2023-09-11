package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.CommentShortDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    List<CommentDto> findCommentsByTextAdmin(String text);

    List<CommentDto> findCommentsByUserAdmin(Long userId);

    void deleteCommentAdmin(Long commentId);

    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    void deleteCommentPrivate(Long userId, Long commentId);

    CommentDto getComment(Long commentId);

    List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size);
}
