package ru.yandex.practicum.compilation.model.dto;

import ru.yandex.practicum.event.model.dto.EventDto;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private long id;
    private List<EventDto> events;
    private boolean pinned;
    private String title;
}