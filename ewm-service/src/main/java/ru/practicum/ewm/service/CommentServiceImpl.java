package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.CommentShortDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;
import ru.practicum.ewm.entity.Comment;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.entity.User;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.exception.DataIsNotCorrectException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CommentDto> findCommentsByTextAdmin(String text, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id").ascending());
        List<Comment> comments = commentRepository.findAllByText(text, pageable);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        return comments.stream()
                .map(CommentMapper::toCommentDto).collect(toList());
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new ObjectNotFoundException("Comment not found"));
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public CommentDto createCommentPrivate(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new DataIsNotCorrectException("Нельзя комментировать неопубликованное событие");
        }
        Comment comment = CommentMapper.toComment(newCommentDto, user, event);
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentPrivate(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findByIdAndAuthor_Id(commentId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Comment not found"));
        comment.setText(updateCommentDto.getText());
        comment.setLastUpdateTime(LocalDateTime.now());
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentPrivate(Long userId, Long commentId) {
        commentRepository.findByIdAndAuthor_Id(commentId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Comment not found"));
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto getCommentPublic(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ObjectNotFoundException("Comment not found"));
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentShortDto> getCommentsByEventPublic(Long eventId, int from, int size) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("last_update_time").descending());
        List<Comment> comments = commentRepository.getAllByEvent_Id(eventId, pageable);
        return comments.stream().map(CommentMapper::toCommentShortDto).collect(toList());
    }
}
