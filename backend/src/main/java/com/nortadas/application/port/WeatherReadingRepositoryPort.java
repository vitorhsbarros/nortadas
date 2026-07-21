package com.nortadas.application.port;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.weatherreading.WeatherReading;
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
}
