package com.nortadas.application.port;

import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.valueobject.BeachId;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for reading {@link Beach} aggregates (docs/architecture.md §1, §8).
 * The application depends on this interface, never on Spring Data; an
 * {@code infrastructure} adapter provides the implementation (Indirection /
 * Protected Variations). Kept read-only for now — the seeded catalogue is the
 * only source of beaches at this stage.
 */
public interface BeachRepositoryPort {

    /** All beaches in the catalogue. */
    List<Beach> findAll();

    /** The beach with the given identity, or empty if none exists. */
    Optional<Beach> findById(BeachId beachId);
}
