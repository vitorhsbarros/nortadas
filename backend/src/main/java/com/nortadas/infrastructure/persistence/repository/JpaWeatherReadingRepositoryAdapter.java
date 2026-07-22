package com.nortadas.infrastructure.persistence.repository;

import com.nortadas.application.port.WeatherReadingRepositoryPort;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.weatherreading.WeatherReading;
import com.nortadas.infrastructure.persistence.datamodel.WeatherReadingDataModel;
import com.nortadas.infrastructure.persistence.mapper.WeatherReadingMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter implementing {@link WeatherReadingRepositoryPort} over Spring Data JPA
 * (Repository + Adapter; docs/architecture.md §7, §8). Translates domain
 * {@link WeatherReading} aggregates to/from
 * {@link com.nortadas.infrastructure.persistence.datamodel.WeatherReadingDataModel}
 * rows via {@link WeatherReadingMapper}, keeping Spring Data types from leaking
 * past the {@code infrastructure} boundary.
 */
@Component
public class JpaWeatherReadingRepositoryAdapter implements WeatherReadingRepositoryPort {

    private final WeatherReadingJpaRepository jpaRepository;
    private final WeatherReadingMapper weatherReadingMapper;

    public JpaWeatherReadingRepositoryAdapter(WeatherReadingJpaRepository jpaRepository,
                                              WeatherReadingMapper weatherReadingMapper) {
        this.jpaRepository = jpaRepository;
        this.weatherReadingMapper = weatherReadingMapper;
    }

    @Override
    public WeatherReading save(WeatherReading reading) {
        WeatherReadingDataModel saved = jpaRepository.save(weatherReadingMapper.toDataModel(reading));
        return weatherReadingMapper.toDomain(saved);
    }

    @Override
    public Optional<WeatherReading> findLatestByBeachId(BeachId beachId) {
        return jpaRepository.findFirstByBeachIdOrderByFetchedAtDesc(beachId.getValue())
                .map(weatherReadingMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to a single bulk {@code DELETE}. Unlike {@link #save} and
     * {@link #findLatestByBeachId}, which run inside Spring Data's built-in
     * per-method transaction, a {@code @Modifying} bulk delete needs one opened
     * explicitly, so this method is {@link Transactional}.
     */
    @Override
    @Transactional
    public int deleteOlderThan(Instant cutoff) {
        return jpaRepository.deleteByFetchedAtBefore(cutoff);
    }
}
