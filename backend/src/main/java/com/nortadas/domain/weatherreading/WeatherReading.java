package com.nortadas.domain.weatherreading;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.valueobject.WeatherCode;
import com.nortadas.domain.valueobject.WeatherCondition;
import com.nortadas.domain.valueobject.WeatherReadingId;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import java.time.Instant;
import java.util.Objects;

/**
 * A single weather observation fetched for a beach: its own identity, the
 * {@link BeachId} it was fetched for, sustained wind speed, wind direction, air
 * temperature, water temperature, the raw WMO {@link WeatherCode}, and when it
 * was fetched.
 *
 * <p>The raw {@link WeatherCode} is stored as the source of truth; the coarse
 * client-facing {@link WeatherCondition} is derived on demand via
 * {@link #getWeatherCondition()}, mirroring how {@link NortadaStatus} is derived
 * from the reading rather than stored.
 *
 * <p>It is its own aggregate root — a per-beach time series that grows without
 * bound, so it references its {@link BeachId} rather than being embedded in the
 * {@code Beach} aggregate, and carries a {@link WeatherReadingId} identity so it
 * can be stored and looked up on its own.
 *
 * <p>It is the sole source of truth a {@link NortadaStatus} is derived from — it
 * does not decide the status itself; the grading rule (US010) lives in a
 * dedicated detection service.
 *
 * <p>Equality is identity-based ({@link WeatherReadingId}), as for any domain
 * entity: two readings are {@code equals} when they share an id, regardless of
 * their other attributes. To compare descriptive state as well, use
 * {@link #sameAs(WeatherReading)}.
 *
 * <p>Construction is factorized in {@link WeatherReadingFactory} (GoF Factory /
 * GRASP Creator): callers outside this package go through
 * {@code WeatherReadingFactory.create}/{@code rehydrate} rather than these
 * constructors directly.
 */
public class WeatherReading {

    private final WeatherReadingId id;
    private final BeachId beachId;
    private final WindSpeed windSpeed;
    private final WindDirection windDirection;
    private final double temperatureCelsius;
    private final double waterTemperatureCelsius;
    private final WeatherCode weatherCode;
    private final Instant fetchedAt;

    /** Creates a new reading, generating its own identity. */
    WeatherReading(BeachId beachId,
                   WindSpeed windSpeed,
                   WindDirection windDirection,
                   double temperatureCelsius,
                   double waterTemperatureCelsius,
                   WeatherCode weatherCode,
                   Instant fetchedAt) {
        this(WeatherReadingId.newId(), beachId, windSpeed, windDirection,
                temperatureCelsius, waterTemperatureCelsius, weatherCode, fetchedAt);
    }

    /** Rehydrates a reading with a known identity (e.g. loaded from persistence). */
    WeatherReading(WeatherReadingId id,
                   BeachId beachId,
                   WindSpeed windSpeed,
                   WindDirection windDirection,
                   double temperatureCelsius,
                   double waterTemperatureCelsius,
                   WeatherCode weatherCode,
                   Instant fetchedAt) {

        if (id == null) {
            throw new IllegalArgumentException("Weather reading id cannot be null!");
        }

        if (beachId == null) {
            throw new IllegalArgumentException("Weather reading beach id cannot be null!");
        }

        if (windSpeed == null) {
            throw new IllegalArgumentException("Weather reading wind speed cannot be null!");
        }

        if (windDirection == null) {
            throw new IllegalArgumentException("Weather reading wind direction cannot be null!");
        }

        if (!Double.isFinite(temperatureCelsius)) {
            throw new IllegalArgumentException("Weather reading temperature must be a finite number!");
        }

        if (!Double.isFinite(waterTemperatureCelsius)) {
            throw new IllegalArgumentException("Weather reading water temperature must be a finite number!");
        }

        if (weatherCode == null) {
            throw new IllegalArgumentException("Weather reading weather code cannot be null!");
        }

        if (fetchedAt == null) {
            throw new IllegalArgumentException("Weather reading fetch time cannot be null!");
        }

        this.id = id;
        this.beachId = beachId;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.temperatureCelsius = temperatureCelsius;
        this.waterTemperatureCelsius = waterTemperatureCelsius;
        this.weatherCode = weatherCode;
        this.fetchedAt = fetchedAt;
    }

    public WeatherReadingId getId() {
        return id;
    }

    public BeachId getBeachId() {
        return beachId;
    }

    public WindSpeed getWindSpeed() {
        return windSpeed;
    }

    public WindDirection getWindDirection() {
        return windDirection;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public double getWaterTemperatureCelsius() {
        return waterTemperatureCelsius;
    }

    public WeatherCode getWeatherCode() {
        return weatherCode;
    }

    /**
     * The coarse client-facing {@link WeatherCondition} derived from this
     * reading's raw {@link WeatherCode} — computed on demand, never stored.
     */
    public WeatherCondition getWeatherCondition() {
        return WeatherCondition.fromWmoCode(weatherCode);
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WeatherReading)) {
            return false;
        }
        WeatherReading that = (WeatherReading) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Attribute-based comparison: {@code true} only when {@code other} is a
     * reading with the same identity <em>and</em> the same beach, wind speed,
     * wind direction, air/water temperature and fetch time (i.e. identical
     * state).
     *
     * <p>Contrast with {@link #equals(Object)}: two readings with the same id but
     * different attributes are {@code equals} (the same entity) yet not
     * {@code sameAs} (different state); readings with different ids are neither.
     * Null-safe — returns {@code false} for a {@code null} argument.
     */
    public boolean sameAs(WeatherReading other) {
        if (other == null) {
            return false;
        }
        return id.equals(other.id)
                && beachId.equals(other.beachId)
                && windSpeed.equals(other.windSpeed)
                && windDirection.equals(other.windDirection)
                && Double.compare(temperatureCelsius, other.temperatureCelsius) == 0
                && Double.compare(waterTemperatureCelsius, other.waterTemperatureCelsius) == 0
                && weatherCode.equals(other.weatherCode)
                && fetchedAt.equals(other.fetchedAt);
    }

    @Override
    public String toString() {
        return "WeatherReading{id=" + id + ", beachId=" + beachId
                + ", windSpeed=" + windSpeed
                + ", windDirection=" + windDirection
                + ", temperatureCelsius=" + temperatureCelsius
                + ", waterTemperatureCelsius=" + waterTemperatureCelsius
                + ", weatherCode=" + weatherCode
                + ", fetchedAt=" + fetchedAt + "}";
    }
}
