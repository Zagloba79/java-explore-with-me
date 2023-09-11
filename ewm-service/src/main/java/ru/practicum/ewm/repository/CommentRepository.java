package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT com " +
            "FROM Comment as com " +
            "WHERE lower(com.text) like concat('%', lower(?1), '%') ")
    List<Comment> findAllByText(String text, Pageable pageable);

    List<Comment> findAllByAuthor_Id(Long userId, Pageable pageable);
}
