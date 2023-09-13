package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.entity.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT com " +
            "FROM Comment as com " +
            "WHERE lower(com.text) like concat('%', lower(?1), '%') ")
    List<Comment> findAllByText(String text, Pageable pageable);

    Optional<Comment> findByIdAndAuthor_Id(Long commentId, Long userId);

    List<Comment> getAllByEvent_Id(Long eventId, Pageable pageable);
}