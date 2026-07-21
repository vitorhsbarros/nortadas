package com.nortadas.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

/**
 * A single weather observation fetched for a beach: sustained wind speed, wind
 * direction, air temperature, water temperature, and when it was fetched.
 *
 * <p>It is the sole source of truth a {@link NortadaStatus} is derived from — it
 * does not decide the status itself; the grading rule (US010) lives in a
 * dedicated detection service.
 */
public final class WeatherReading {

    private final BeachId beachId;
    private final WindSpeed windSpeed;
    private final WindDirection windDirection;
    private final double temperatureCelsius;
    private final double waterTemperatureCelsius;
    private final Instant fetchedAt;

    public WeatherReading(BeachId beachId,
                          WindSpeed windSpeed,
                          WindDirection windDirection,
                          double temperatureCelsius,
                          double waterTemperatureCelsius,
                          Instant fetchedAt) {

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

        if (fetchedAt == null) {
            throw new IllegalArgumentException("Weather reading fetch time cannot be null!");
        }

        this.beachId = beachId;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.temperatureCelsius = temperatureCelsius;
        this.waterTemperatureCelsius = waterTemperatureCelsius;
        this.fetchedAt = fetchedAt;
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
        return beachId.equals(that.beachId)
                && windSpeed.equals(that.windSpeed)
                && windDirection.equals(that.windDirection)
                && Double.compare(temperatureCelsius, that.temperatureCelsius) == 0
                && Double.compare(waterTemperatureCelsius, that.waterTemperatureCelsius) == 0
                && fetchedAt.equals(that.fetchedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beachId, windSpeed, windDirection, temperatureCelsius, waterTemperatureCelsius, fetchedAt);
    }

    @Override
    public String toString() {
        return "WeatherReading{beachId=" + beachId + ", windSpeed=" + windSpeed
                + ", windDirection=" + windDirection
                + ", temperatureCelsius=" + temperatureCelsius
                + ", waterTemperatureCelsius=" + waterTemperatureCelsius
                + ", fetchedAt=" + fetchedAt + "}";
    }
}
