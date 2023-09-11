package ru.practicum.ewm.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comments")
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String text;
    @OneToOne
    @JoinColumn(nullable = false, name = "author_id", referencedColumnName = "id")
    private User author;
    @OneToOne
    @JoinColumn(nullable = false, name = "event_id", referencedColumnName = "id")
    private Event event;
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;
}