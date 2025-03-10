package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false, unique = true, length = 50)
    @EqualsAndHashCode.Include
    private String title;

    @ElementCollection
    @CollectionTable(name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation_id"))
    @Column(name = "event_id")
    private List<Long> eventIds;
}
