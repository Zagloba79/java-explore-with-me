package ru.practicum.ewm.entity;

import lombok.*;
import ru.practicum.ewm.enums.Status;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "requests")
@AllArgsConstructor
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime created;
    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
    private Event event;
    @OneToOne
    @JoinColumn(name = "requester_id", referencedColumnName = "id", nullable = false)
    private User requester;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public Request(LocalDateTime created, Event event, User requester, Status status) {
        this.created = created;
        this.event = event;
        this.requester = requester;
        this.status = status;
    }
}
