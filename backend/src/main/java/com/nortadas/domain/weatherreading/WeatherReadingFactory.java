package com.nortadas.domain.weatherreading;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.WeatherCode;
import com.nortadas.domain.valueobject.WeatherReadingId;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import java.time.Instant;

/**
 * Factorizes {@link WeatherReading} construction (GoF Factory, GRASP Creator;
 * docs/architecture.md §6, §7): the sole public entry point for building
 * readings from outside this package, so callers never have to choose between
 * {@code WeatherReading}'s create/rehydrate constructors themselves.
 */
public final class WeatherReadingFactory {

    private WeatherReadingFactory() {
    }

    /** Creates a new reading, generating its own identity (for fresh fetches). */
    public static WeatherReading create(BeachId beachId,
                                        WindSpeed windSpeed,
                                        WindDirection windDirection,
                                        double temperatureCelsius,
                                        double waterTemperatureCelsius,
                                        WeatherCode weatherCode,
                                        Instant fetchedAt) {
        return new WeatherReading(
                beachId, windSpeed, windDirection, temperatureCelsius, waterTemperatureCelsius,
                weatherCode, fetchedAt);
    }

    /** Rehydrates a reading with a known identity (e.g. loaded from persistence). */
    public static WeatherReading rehydrate(WeatherReadingId id,
                                           BeachId beachId,
                                           WindSpeed windSpeed,
                                           WindDirection windDirection,
                                           double temperatureCelsius,
                                           double waterTemperatureCelsius,
                                           WeatherCode weatherCode,
                                           Instant fetchedAt) {
        return new WeatherReading(
                id, beachId, windSpeed, windDirection, temperatureCelsius, waterTemperatureCelsius,
                weatherCode, fetchedAt);
    }
}
