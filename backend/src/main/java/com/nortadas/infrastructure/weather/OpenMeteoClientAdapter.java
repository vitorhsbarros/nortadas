package com.nortadas.infrastructure.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nortadas.application.port.WeatherClientPort;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import com.nortadas.domain.weatherreading.WeatherReading;
import com.nortadas.domain.weatherreading.WeatherReadingFactory;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Open-Meteo implementation of {@link WeatherClientPort} (Adapter; ADR-004,
 * docs/architecture.md §7). Hides the provider's JSON shape behind the port so
 * it can change without rippling inward (Protected Variations).
 *
 * <p>Open-Meteo splits the data this project needs across two endpoints, so the
 * adapter makes <strong>two calls per beach</strong> and combines them into one
 * {@link WeatherReading}:
 * <ol>
 *   <li>the standard <em>forecast</em> API for wind speed (km/h), wind direction
 *       and air temperature ({@code temperature_2m}); and</li>
 *   <li>the separate <em>marine</em> API for sea-surface temperature, which the
 *       forecast API does not provide.</li>
 * </ol>
 *
 * <p>Both base URLs are configurable ({@code nortadas.weather.open-meteo.base-url}
 * and {@code nortadas.weather.open-meteo.marine-base-url}); neither is hardcoded.
 */
@Component
public class OpenMeteoClientAdapter implements WeatherClientPort {

    private final RestClient forecastClient;
    private final RestClient marineClient;

    public OpenMeteoClientAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${nortadas.weather.open-meteo.base-url}") String baseUrl,
            @Value("${nortadas.weather.open-meteo.marine-base-url}") String marineBaseUrl) {
        this.forecastClient = restClientBuilder.clone().baseUrl(baseUrl).build();
        this.marineClient = restClientBuilder.clone().baseUrl(marineBaseUrl).build();
    }

    @Override
    public WeatherReading fetchCurrent(BeachId beachId, Latitude latitude, Longitude longitude) {
        ForecastResponse forecast = fetchForecast(latitude, longitude);
        MarineResponse marine = fetchMarine(latitude, longitude);

        ForecastCurrent current = requireCurrent(forecast == null ? null : forecast.current(),
                "forecast");
        MarineCurrent marineCurrent = requireCurrent(marine == null ? null : marine.current(),
                "marine");

        Double waterTemperature = marineCurrent.seaSurfaceTemperature();
        if (waterTemperature == null) {
            throw new IllegalStateException("Open-Meteo marine response had no sea-surface temperature");
        }

        return WeatherReadingFactory.create(
                beachId,
                new WindSpeed(current.windSpeed()),
                new WindDirection(normalizeDirection(current.windDirection())),
                current.airTemperature(),
                waterTemperature,
                Instant.now());
    }

    private ForecastResponse fetchForecast(Latitude latitude, Longitude longitude) {
        return forecastClient.get()
                .uri(uriBuilder -> uriBuilder.path("/forecast")
                        .queryParam("latitude", latitude.getDegrees())
                        .queryParam("longitude", longitude.getDegrees())
                        .queryParam("current", "wind_speed_10m,wind_direction_10m,temperature_2m")
                        .queryParam("wind_speed_unit", "kmh")
                        .build())
                .retrieve()
                .body(ForecastResponse.class);
    }

    private MarineResponse fetchMarine(Latitude latitude, Longitude longitude) {
        return marineClient.get()
                .uri(uriBuilder -> uriBuilder.path("/marine")
                        .queryParam("latitude", latitude.getDegrees())
                        .queryParam("longitude", longitude.getDegrees())
                        .queryParam("current", "sea_surface_temperature")
                        .build())
                .retrieve()
                .body(MarineResponse.class);
    }

    private static <T> T requireCurrent(T current, String which) {
        if (current == null) {
            throw new IllegalStateException("Open-Meteo " + which + " response had no current block");
        }
        return current;
    }

    /** Open-Meteo may report North as 360°; the domain expects [0, 360). */
    private static double normalizeDirection(double degrees) {
        double normalized = degrees % 360.0;
        if (normalized < 0.0) {
            normalized += 360.0;
        }
        return normalized;
    }

    /** JSON shape of the Open-Meteo forecast response (only the fields we read). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ForecastResponse(@JsonProperty("current") ForecastCurrent current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ForecastCurrent(
            @JsonProperty("wind_speed_10m") double windSpeed,
            @JsonProperty("wind_direction_10m") double windDirection,
            @JsonProperty("temperature_2m") double airTemperature) {
    }

    /** JSON shape of the Open-Meteo marine response (only the fields we read). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record MarineResponse(@JsonProperty("current") MarineCurrent current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MarineCurrent(@JsonProperty("sea_surface_temperature") Double seaSurfaceTemperature) {
    }
}
