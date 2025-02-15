package ru.yandex.practicum.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.category.model.dto.CategoryDto;
import ru.yandex.practicum.location.model.dto.LocationDto;
import ru.yandex.practicum.state.State;
import ru.yandex.practicum.user.model.dto.UserWithoutEmailDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
        private long id;
        private String annotation;
        private CategoryDto category;
        private int confirmedRequests;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdOn;

        private String description;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventDate;

        private UserWithoutEmailDto initiator;
        private LocationDto location;
        private boolean paid;
        private int participantLimit;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime publishedOn;

        private boolean requestModeration;
        private State state;
        private String title;
        private long views;
}
