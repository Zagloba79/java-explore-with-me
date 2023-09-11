package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.CommentShortDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;
import ru.practicum.ewm.entity.Comment;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.entity.User;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.util.InfoFromRep;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final InfoFromRep infoFromRep;

    @Override
    public List<CommentDto> findCommentsByTextAdmin(String text, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id").ascending());
        List<Comment> comments = commentRepository.findAllByText(text, pageable);
        List<Event> events = comments.stream().map(Comment::getEvent).collect(toList());
        Map<Long, Long> viewsFromRep = infoFromRep.getViewsFromStat(events);
        return comments.stream()
                .map(comment -> CommentMapper
                        .toCommentDto(comment, viewsFromRep.get(comment.getEvent().getId()))).collect(toList());
    }

    @Override
    public List<CommentDto> findCommentsByUserAdmin(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id").ascending());
        List<Comment> comments = commentRepository.findAllByAuthor_Id(userId, pageable);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        List<Event> events = comments.stream().map(Comment::getEvent).collect(toList());
        Map<Long, Long> viewsFromRep = infoFromRep.getViewsFromStat(events);
        return comments.stream()
                .map(comment -> CommentMapper
                        .toCommentDto(comment, viewsFromRep.get(comment.getEvent().getId()))).collect(toList());
    }

    @Override
    public void deleteCommentAdmin(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new ObjectNotFoundException("Comment not found"));
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto createCommentPrivate(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        Comment comment = CommentMapper.toComment(newCommentDto, user, event);
        commentRepository.save(comment);
        Map<Long, Long> viewsFromRep = infoFromRep.getViewsFromStat(List.of(event));
        return CommentMapper.toCommentDto(comment, viewsFromRep.get(event.getId()));
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        return null;
    }

    @Override
    public void deleteCommentPrivate(Long userId, Long commentId) {

    }

    @Override
    public CommentDto getComment(Long commentId) {
        return null;
    }

    @Override
    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
        return null;
    }
}
