package com.nortadas.infrastructure.persistence.repository;

import com.nortadas.infrastructure.persistence.datamodel.WeatherReadingDataModel;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository over {@link WeatherReadingDataModel}. Confined to
 * {@code infrastructure}; the application talks to
 * {@link com.nortadas.application.port.WeatherReadingRepositoryPort} instead
 * (docs/architecture.md §8).
 */
public interface WeatherReadingJpaRepository extends JpaRepository<WeatherReadingDataModel, UUID> {

    /** The most recent reading for a beach, or empty if none has been stored yet. */
    Optional<WeatherReadingDataModel> findFirstByBeachIdOrderByFetchedAtDesc(UUID beachId);

    /**
     * Deletes, in a single bulk {@code DELETE} statement, every reading fetched
     * strictly before {@code cutoff} (issue #48). Expressed as a JPQL
     * {@code @Modifying} query rather than a derived {@code deleteBy...} name so
     * it issues one set-based statement instead of loading rows into memory to
     * remove them one by one. Requires an active transaction, supplied by the
     * calling adapter.
     *
     * @param cutoff the exclusive lower bound
     * @return the number of rows deleted
     */
    @Modifying
    @Query("delete from WeatherReadingDataModel w where w.fetchedAt < :cutoff")
    int deleteByFetchedAtBefore(@Param("cutoff") Instant cutoff);
}
