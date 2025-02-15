package ru.yandex.practicum.compilation.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.compilation.model.dto.CompilationDto;
import ru.yandex.practicum.compilation.model.dto.CreateCompilationDto;
import ru.yandex.practicum.compilation.model.dto.UpdateCompilationDto;

@RequestMapping("/admin/compilations")
public interface AdminCompilationApi {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CompilationDto create(@RequestBody @Valid final CreateCompilationDto createCompilationDto);

    @PatchMapping("/{compId}")
    CompilationDto update(@RequestBody @Valid final UpdateCompilationDto updateCompilationDto,
                                 @PathVariable @Positive final long compId);

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCompilation(@PathVariable @Positive final long compId);
}
