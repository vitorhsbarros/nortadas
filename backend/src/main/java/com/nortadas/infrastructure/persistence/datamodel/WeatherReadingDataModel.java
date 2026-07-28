package com.nortadas.infrastructure.persistence.datamodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA data model for the {@code weather_reading} table (docs/architecture.md §3,
 * §8), mapped to/from the pure-domain
 * {@link com.nortadas.domain.weatherreading.WeatherReading} by
 * {@link com.nortadas.infrastructure.persistence.mapper.WeatherReadingMapper}.
 *
 * <p>The owning beach is referenced by its id ({@code beach_id}) rather than a
 * JPA association: a weather reading is its own aggregate and only needs the
 * {@code BeachId} to rebuild the domain object, so it holds the raw foreign-key
 * value instead of a {@code @ManyToOne} to {@link BeachDataModel}.
 */
@Entity
@Table(name = "weather_reading")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WeatherReadingDataModel {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "beach_id", nullable = false)
    private UUID beachId;

    @Column(name = "wind_speed", nullable = false)
    private double windSpeed;

    @Column(name = "wind_direction", nullable = false)
    private double windDirection;

    @Column(name = "temperature_celsius", nullable = false)
    private double temperatureCelsius;

    @Column(name = "water_temperature_celsius", nullable = false)
    private double waterTemperatureCelsius;

    @Column(name = "weather_code", nullable = false)
    private int weatherCode;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}
