package com.nortadas.domain;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherReadingTest {

    private static final BeachId BEACH_ID = BeachId.newId();
    private static final WindSpeed SPEED = new WindSpeed(30.0);
    private static final WindDirection DIRECTION = new WindDirection(350.0);
    private static final double TEMPERATURE = 21.5;
    private static final Instant FETCHED_AT = Instant.parse("2026-07-20T12:00:00Z");

    private static WeatherReading reading() {
        return new WeatherReading(BEACH_ID, SPEED, DIRECTION, TEMPERATURE, FETCHED_AT);
    }

    // --- creation --------------------------------------------------------

    @Test
    void keepsAllAttributes() {
        WeatherReading reading = reading();
        assertEquals(BEACH_ID, reading.getBeachId());
        assertEquals(SPEED, reading.getWindSpeed());
        assertEquals(DIRECTION, reading.getWindDirection());
        assertEquals(TEMPERATURE, reading.getTemperatureCelsius());
        assertEquals(FETCHED_AT, reading.getFetchedAt());
    }

    @Test
    @DisplayName("accepts negative and extreme finite temperatures")
    void acceptsExtremeFiniteTemperatures() {
        assertEquals(-15.0,
                new WeatherReading(BEACH_ID, SPEED, DIRECTION, -15.0, FETCHED_AT)
                        .getTemperatureCelsius());
        assertEquals(-Double.MAX_VALUE,
                new WeatherReading(BEACH_ID, SPEED, DIRECTION, -Double.MAX_VALUE, FETCHED_AT)
                        .getTemperatureCelsius());
    }

    // --- invariants ------------------------------------------------------

    @Test
    void rejectsNullBeachId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(null, SPEED, DIRECTION, TEMPERATURE, FETCHED_AT));
        assertEquals("Weather reading beach id cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullWindSpeed() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(BEACH_ID, null, DIRECTION, TEMPERATURE, FETCHED_AT));
        assertEquals("Weather reading wind speed cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullWindDirection() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(BEACH_ID, SPEED, null, TEMPERATURE, FETCHED_AT));
        assertEquals("Weather reading wind direction cannot be null!", ex.getMessage());
    }

    @ParameterizedTest(name = "rejects non-finite temperature {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteTemperature(double temperature) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(BEACH_ID, SPEED, DIRECTION, temperature, FETCHED_AT));
        assertEquals("Weather reading temperature must be a finite number!", ex.getMessage());
    }

    @Test
    void rejectsNullFetchTime() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(BEACH_ID, SPEED, DIRECTION, TEMPERATURE, null));
        assertEquals("Weather reading fetch time cannot be null!", ex.getMessage());
    }

    // --- value-based equality --------------------------------------------

    @Test
    void equalsIsReflexive() {
        WeatherReading reading = reading();
        assertEquals(reading, reading);
    }

    @Test
    void readingsWithIdenticalFieldsAreEqualWithMatchingHashCodes() {
        assertEquals(reading(), reading());
        assertEquals(reading().hashCode(), reading().hashCode());
    }

    @Test
    void differsWhenBeachIdDiffers() {
        assertNotEquals(reading(),
                new WeatherReading(BeachId.newId(), SPEED, DIRECTION, TEMPERATURE, FETCHED_AT));
    }

    @Test
    void differsWhenWindSpeedDiffers() {
        assertNotEquals(reading(),
                new WeatherReading(BEACH_ID, new WindSpeed(31.0), DIRECTION, TEMPERATURE, FETCHED_AT));
    }

    @Test
    void differsWhenWindDirectionDiffers() {
        assertNotEquals(reading(),
                new WeatherReading(BEACH_ID, SPEED, new WindDirection(10.0), TEMPERATURE, FETCHED_AT));
    }

    @Test
    void differsWhenTemperatureDiffers() {
        assertNotEquals(reading(),
                new WeatherReading(BEACH_ID, SPEED, DIRECTION, TEMPERATURE + 0.1, FETCHED_AT));
    }

    @Test
    void differsWhenFetchTimeDiffers() {
        assertNotEquals(reading(),
                new WeatherReading(BEACH_ID, SPEED, DIRECTION, TEMPERATURE,
                        FETCHED_AT.plusSeconds(1)));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        WeatherReading reading = reading();
        assertNotEquals(reading, null);
        assertNotEquals(reading, "reading");
    }

    // --- toString --------------------------------------------------------

    @Test
    void toStringContainsAllFields() {
        String rendered = reading().toString();
        assertTrue(rendered.contains(BEACH_ID.toString()));
        assertTrue(rendered.contains("30.0 km/h"));
        assertTrue(rendered.contains("350.0°"));
        assertTrue(rendered.contains("21.5"));
        assertTrue(rendered.contains("2026-07-20T12:00:00Z"));
    }
}
