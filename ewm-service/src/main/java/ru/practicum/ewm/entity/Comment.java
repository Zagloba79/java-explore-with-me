package ru.practicum.ewm.entity;

import lombok.*;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.UserDto;

import javax.persistence.*;

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
    private UserDto author;
    @OneToOne
    @JoinColumn(nullable = false, name = "event_id", referencedColumnName = "id")
    private EventShortDto eventDto;
    @Column(name = "create_time", nullable = false)
    private String createTime;
    @Column(name = "last_update_time")
    private String lastUpdateTime;
}