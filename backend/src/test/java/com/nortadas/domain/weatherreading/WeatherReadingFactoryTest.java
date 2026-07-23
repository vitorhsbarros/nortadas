package com.nortadas.domain.weatherreading;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.WeatherCode;
import com.nortadas.domain.valueobject.WeatherReadingId;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WeatherReadingFactoryTest {

    private static final BeachId BEACH_ID = BeachId.newId();
    private static final WindSpeed SPEED = new WindSpeed(30.0);
    private static final WindDirection DIRECTION = new WindDirection(350.0);
    private static final double TEMPERATURE = 21.5;
    private static final double WATER_TEMPERATURE = 18.5;
    private static final WeatherCode WEATHER_CODE = new WeatherCode(3);
    private static final Instant FETCHED_AT = Instant.parse("2026-07-20T12:00:00Z");

    @Test
    @DisplayName("create generates a fresh identity")
    void createGeneratesFreshIdentity() {
        WeatherReading reading = WeatherReadingFactory.create(
                BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, WEATHER_CODE, FETCHED_AT);

        assertNotNull(reading.getId());
        assertEquals(BEACH_ID, reading.getBeachId());
        assertEquals(SPEED, reading.getWindSpeed());
        assertEquals(DIRECTION, reading.getWindDirection());
        assertEquals(TEMPERATURE, reading.getTemperatureCelsius());
        assertEquals(WATER_TEMPERATURE, reading.getWaterTemperatureCelsius());
        assertEquals(WEATHER_CODE, reading.getWeatherCode());
        assertEquals(FETCHED_AT, reading.getFetchedAt());
    }

    @Test
    @DisplayName("rehydrate keeps the given identity")
    void rehydrateKeepsGivenIdentity() {
        WeatherReadingId id = WeatherReadingId.newId();

        WeatherReading reading = WeatherReadingFactory.rehydrate(
                id, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, WEATHER_CODE, FETCHED_AT);

        assertEquals(id, reading.getId());
        assertEquals(BEACH_ID, reading.getBeachId());
        assertEquals(WEATHER_CODE, reading.getWeatherCode());
        assertEquals(FETCHED_AT, reading.getFetchedAt());
    }
}
