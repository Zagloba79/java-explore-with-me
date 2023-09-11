package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CommentDto;
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

    public static CommentDto toCommentDto(Comment comment, Long viewsFromRep) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toUserDto(comment.getAuthor()))
                .eventDto(EventMapper.toEventShortDto(comment.getEvent(), viewsFromRep))
                .createTime(comment.getCreateTime().format(FORMATTER))
                .lastUpdateTime(comment.getLastUpdateTime().format(FORMATTER))
                .build();
    }
}