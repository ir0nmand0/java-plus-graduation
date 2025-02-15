package ru.yandex.practicum.compilation.api;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.compilation.model.dto.CompilationDto;

import java.util.List;

@RequestMapping("/compilations")
public interface PublicCompilationApi {
    @GetMapping
    List<CompilationDto> getCompilations(@RequestParam(required = false) final Boolean pinned,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
                                                @RequestParam(defaultValue = "10") @Positive final int size);

    @GetMapping("/{compId}")
    CompilationDto getCompilation(@PathVariable @Positive final long compId);
}
