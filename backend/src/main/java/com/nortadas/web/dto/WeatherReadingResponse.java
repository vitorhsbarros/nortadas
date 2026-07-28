package com.nortadas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Plain value DTO for the {@code reading} block of a beach detail response
 * ({@code GET /api/beaches/{id}}, US012): the beach's latest stored weather
 * reading, shaped for the wire. HAL-agnostic (it is nested inside a
 * {@link BeachResponse}, not a resource of its own) and independent of the
 * domain {@code WeatherReading} and the JPA data model (docs/architecture.md §3).
 *
 * <p>Deliberately <strong>not</strong> part of the list endpoint
 * ({@code GET /api/beaches}, US011) — only the detail view carries a reading.
 */
@Getter
@AllArgsConstructor
public class WeatherReadingResponse {

    /** Wind speed in kilometres per hour. */
    private final double windSpeed;

    /** Wind direction in degrees (meteorological, 0–360). */
    private final double windDirection;

    /** Air temperature in degrees Celsius. */
    private final double temperature;

    /** Sea-surface (water) temperature in degrees Celsius. */
    private final double waterTemperature;

    /** Raw WMO weather-interpretation code ({@code ww} code, 0–99). */
    private final int weatherCode;

    /** When the reading was fetched, as an ISO-8601 instant string. */
    private final String fetchedAt;
}
