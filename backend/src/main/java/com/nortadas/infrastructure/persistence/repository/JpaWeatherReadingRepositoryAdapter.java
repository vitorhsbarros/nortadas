package com.nortadas.infrastructure.persistence.repository;

import com.nortadas.application.port.WeatherReadingRepositoryPort;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.weatherreading.WeatherReading;
import com.nortadas.infrastructure.persistence.datamodel.WeatherReadingDataModel;
import com.nortadas.infrastructure.persistence.mapper.WeatherReadingMapper;
import java.util.Optional;
import org.springframework.stereotype.Component;

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
}
