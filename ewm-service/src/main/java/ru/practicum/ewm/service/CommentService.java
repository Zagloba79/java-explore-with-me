package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.CommentShortDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    List<CommentDto> findCommentsByTextAdmin(String text, int from, int size);

    CommentDto updateCommentAdmin(UpdateCommentDto updateCommentDto);

    void deleteCommentAdmin(Long commentId);

    CommentDto createCommentPrivate(Long userId, NewCommentDto newCommentDto);

    List<CommentDto> getCommentsByEventPrivate(Long userId, Long eventId);

    CommentDto getCommentPrivate(Long userId, Long eventId);

    CommentDto updateCommentPrivate(Long userId, UpdateCommentDto updateCommentDto);

    void deleteCommentPrivate(Long userId, Long commentId);

    CommentDto getCommentPublic(Long commentId);

    List<CommentShortDto> getCommentsByEventPublic(Long eventId);

    List<CommentDto> findCommentsByAuthorAdmin(Long userId);
}
