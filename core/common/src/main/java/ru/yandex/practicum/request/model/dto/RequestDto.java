package ru.yandex.practicum.request.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.state.State;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
        private long id;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime created;

        private long event;
        private long requester;
        private State status;
}
