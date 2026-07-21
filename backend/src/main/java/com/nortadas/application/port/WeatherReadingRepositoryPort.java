package com.nortadas.application.port;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.weatherreading.WeatherReading;
import java.time.Instant;
import java.util.Optional;

/**
 * Outbound port for persisting and reading {@link WeatherReading} aggregates
 * (docs/architecture.md §1, §8). The application depends on this interface, never
 * on Spring Data; an {@code infrastructure} adapter provides the implementation
 * (Indirection / Protected Variations).
 */
public interface WeatherReadingRepositoryPort {

    /** Stores a freshly fetched reading, returning the persisted aggregate. */
    WeatherReading save(WeatherReading reading);

    /** The most recent reading stored for a beach, or empty if none exists yet. */
    Optional<WeatherReading> findLatestByBeachId(BeachId beachId);

    /**
     * Bulk-deletes every reading fetched strictly before {@code cutoff},
     * returning how many rows were removed (for logging). This enforces the
     * rolling retention window (issue #48): it is a strict window with no
     * keep-the-latest-per-beach exception, so a beach whose readings are all
     * older than the cutoff is left with none.
     *
     * @param cutoff the exclusive lower bound; readings with
     *               {@code fetchedAt < cutoff} are deleted
     * @return the number of readings deleted
     */
    int deleteOlderThan(Instant cutoff);
}
