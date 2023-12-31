package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.CommentShortDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.entity.Comment;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class CommentMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Comment toComment(NewCommentDto newCommentDto, User user, Event event) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .author(user)
                .event(event)
                .createTime(LocalDateTime.now())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toUserDto(comment.getAuthor()))
                .event(EventMapper.toEventCommentDto(comment.getEvent()))
                .createTime(comment.getCreateTime().format(FORMATTER))
                .build();
        if (comment.getLastUpdateTime() != null) {
            commentDto.setLastUpdateTime(comment.getLastUpdateTime().format(FORMATTER));
        }
        return commentDto;
    }

    public static CommentShortDto toCommentShortDto(Comment comment) {
        CommentShortDto commentShortDto = CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toUserDto(comment.getAuthor()))
                .eventDto(EventMapper.toEventCommentDto(comment.getEvent()))
                .build();
        if (comment.getLastUpdateTime() != null) {
            commentShortDto.setLastUpdateTime(comment.getLastUpdateTime().format(FORMATTER));
        }
        return commentShortDto;
    }
}