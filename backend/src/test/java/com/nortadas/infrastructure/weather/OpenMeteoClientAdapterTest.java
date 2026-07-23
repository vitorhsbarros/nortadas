package com.nortadas.infrastructure.weather;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.WeatherCondition;
import com.nortadas.domain.weatherreading.WeatherReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Unit tests for {@link OpenMeteoClientAdapter}: the provider's two-endpoint JSON
 * is stubbed with {@link MockRestServiceServer} (no live network) so the tests pin
 * down the JSON → domain mapping, the two-call fan-out, wind-direction
 * normalization and the documented failure modes.
 */
class OpenMeteoClientAdapterTest {

    private static final String FORECAST_BASE = "http://forecast.test";
    private static final String MARINE_BASE = "http://marine.test";

    private static final BeachId BEACH_ID = BeachId.newId();
    private static final Latitude LATITUDE = new Latitude(41.18);
    private static final Longitude LONGITUDE = new Longitude(-8.7);

    private MockRestServiceServer server;
    private OpenMeteoClientAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new OpenMeteoClientAdapter(builder, FORECAST_BASE, MARINE_BASE);
    }

    private void expectForecast(String json) {
        server.expect(requestTo(containsString(FORECAST_BASE + "/forecast")))
                .andExpect(queryParam("latitude", "41.18"))
                .andExpect(queryParam("longitude", "-8.7"))
                .andExpect(queryParam("current", "wind_speed_10m,wind_direction_10m,temperature_2m,weather_code"))
                .andExpect(queryParam("wind_speed_unit", "kmh"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private void expectMarine(String json) {
        server.expect(requestTo(containsString(MARINE_BASE + "/marine")))
                .andExpect(queryParam("latitude", "41.18"))
                .andExpect(queryParam("longitude", "-8.7"))
                .andExpect(queryParam("current", "sea_surface_temperature"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private static String forecastJson(double windSpeed, double windDirection, double airTemp) {
        return forecastJson(windSpeed, windDirection, airTemp, 0);
    }

    private static String forecastJson(double windSpeed, double windDirection, double airTemp, int weatherCode) {
        return "{\"current\":{\"wind_speed_10m\":" + windSpeed
                + ",\"wind_direction_10m\":" + windDirection
                + ",\"temperature_2m\":" + airTemp
                + ",\"weather_code\":" + weatherCode + "}}";
    }

    private static String marineJson(String seaSurfaceTemperature) {
        return "{\"current\":{\"sea_surface_temperature\":" + seaSurfaceTemperature + "}}";
    }

    @Test
    @DisplayName("maps both endpoints into one reading and calls both configured base URLs")
    void mapsBothEndpointsIntoOneReading() {
        // 61 is the WMO code for rain, so the derived condition is RAIN.
        expectForecast(forecastJson(32.5, 350.0, 21.4, 61));
        expectMarine(marineJson("18.2"));

        WeatherReading reading = adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE);

        assertEquals(BEACH_ID, reading.getBeachId());
        assertEquals(32.5, reading.getWindSpeed().getKmPerHour());
        assertEquals(350.0, reading.getWindDirection().getDegrees());
        assertEquals(21.4, reading.getTemperatureCelsius());
        assertEquals(18.2, reading.getWaterTemperatureCelsius());
        assertEquals(61, reading.getWeatherCode().getValue());
        assertEquals(WeatherCondition.RAIN, reading.getWeatherCondition());
        server.verify();
    }

    @Test
    @DisplayName("normalizes a 360 degree wind direction to 0")
    void normalizesThreeSixtyToZero() {
        expectForecast(forecastJson(10.0, 360.0, 15.0));
        expectMarine(marineJson("16.0"));

        WeatherReading reading = adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE);

        assertEquals(0.0, reading.getWindDirection().getDegrees());
        server.verify();
    }

    @Test
    @DisplayName("normalizes a negative wind direction back into [0, 360)")
    void normalizesNegativeDirection() {
        expectForecast(forecastJson(10.0, -10.0, 15.0));
        expectMarine(marineJson("16.0"));

        WeatherReading reading = adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE);

        assertEquals(350.0, reading.getWindDirection().getDegrees());
        server.verify();
    }

    @Test
    @DisplayName("throws IllegalStateException when the marine response has no sea-surface temperature")
    void throwsWhenSeaSurfaceTemperatureMissing() {
        expectForecast(forecastJson(20.0, 200.0, 19.0));
        expectMarine(marineJson("null"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE));

        assertTrue(ex.getMessage().contains("sea-surface temperature"));
        server.verify();
    }

    @Test
    @DisplayName("throws IllegalStateException when the forecast response has no current block")
    void throwsWhenForecastCurrentMissing() {
        expectForecast("{}");
        expectMarine(marineJson("16.0"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE));

        assertTrue(ex.getMessage().contains("forecast"));
    }

    @Test
    @DisplayName("throws IllegalStateException when the marine response has no current block")
    void throwsWhenMarineCurrentMissing() {
        expectForecast(forecastJson(20.0, 200.0, 19.0));
        expectMarine("{}");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE));

        assertTrue(ex.getMessage().contains("marine"));
    }

    @Test
    @DisplayName("throws IllegalStateException when the forecast body is entirely empty")
    void throwsWhenForecastBodyEmpty() {
        server.expect(requestTo(containsString(FORECAST_BASE + "/forecast")))
                .andRespond(withSuccess());
        expectMarine(marineJson("16.0"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE));

        assertTrue(ex.getMessage().contains("forecast"));
    }

    @Test
    @DisplayName("throws IllegalStateException when the marine body is entirely empty")
    void throwsWhenMarineBodyEmpty() {
        expectForecast(forecastJson(20.0, 200.0, 19.0));
        server.expect(requestTo(containsString(MARINE_BASE + "/marine")))
                .andRespond(withSuccess());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE));

        assertTrue(ex.getMessage().contains("marine"));
    }
}
