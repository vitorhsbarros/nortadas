package com.nortadas.infrastructure.persistence.repository;

import com.nortadas.infrastructure.persistence.datamodel.WeatherReadingDataModel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository over {@link WeatherReadingDataModel}. Confined to
 * {@code infrastructure}; the application talks to
 * {@link com.nortadas.application.port.WeatherReadingRepositoryPort} instead
 * (docs/architecture.md §8).
 */
public interface WeatherReadingJpaRepository extends JpaRepository<WeatherReadingDataModel, UUID> {

    /** The most recent reading for a beach, or empty if none has been stored yet. */
    Optional<WeatherReadingDataModel> findFirstByBeachIdOrderByFetchedAtDesc(UUID beachId);
}
