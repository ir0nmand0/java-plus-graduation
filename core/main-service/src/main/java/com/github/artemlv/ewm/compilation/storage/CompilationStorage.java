package com.github.artemlv.ewm.compilation.storage;

import com.github.artemlv.ewm.compilation.model.Compilation;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface CompilationStorage {
    Compilation getByIdOrElseThrow(final long id);

    Optional<Compilation> getById(final long id);

    Compilation save(final Compilation compilation);

    void existsByIdOrElseThrow(final long id);

    void deleteById(final long id);

    List<Compilation> findAllByPinnedIs(final boolean pinned, final PageRequest page);

    List<Compilation> findAll(final PageRequest page);
}
