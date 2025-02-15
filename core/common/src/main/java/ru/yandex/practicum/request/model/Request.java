package ru.yandex.practicum.request.model;

import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.state.State;
import ru.yandex.practicum.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "request")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private LocalDateTime created;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private State status;
}
