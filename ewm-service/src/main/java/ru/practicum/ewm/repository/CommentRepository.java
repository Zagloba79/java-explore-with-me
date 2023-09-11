package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
