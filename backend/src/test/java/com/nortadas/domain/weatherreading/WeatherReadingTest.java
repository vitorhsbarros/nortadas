package com.nortadas.domain.weatherreading;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.WeatherReadingId;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherReadingTest {

    private static final WeatherReadingId ID = WeatherReadingId.newId();
    private static final BeachId BEACH_ID = BeachId.newId();
    private static final WindSpeed SPEED = new WindSpeed(30.0);
    private static final WindDirection DIRECTION = new WindDirection(350.0);
    private static final double TEMPERATURE = 21.5;
    private static final double WATER_TEMPERATURE = 18.5;
    private static final Instant FETCHED_AT = Instant.parse("2026-07-20T12:00:00Z");

    private static WeatherReading rehydrated() {
        return new WeatherReading(ID, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT);
    }

    // --- creation --------------------------------------------------------

    @Test
    void keepsAllAttributes() {
        WeatherReading reading = rehydrated();
        assertEquals(ID, reading.getId());
        assertEquals(BEACH_ID, reading.getBeachId());
        assertEquals(SPEED, reading.getWindSpeed());
        assertEquals(DIRECTION, reading.getWindDirection());
        assertEquals(TEMPERATURE, reading.getTemperatureCelsius());
        assertEquals(WATER_TEMPERATURE, reading.getWaterTemperatureCelsius());
        assertEquals(FETCHED_AT, reading.getFetchedAt());
    }

    @Test
    @DisplayName("the generating constructor assigns a fresh identity")
    void generatingConstructorAssignsIdentity() {
        WeatherReading reading =
                new WeatherReading(BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT);
        assertNotNull(reading.getId());
    }

    @Test
    @DisplayName("accepts negative and extreme finite air temperatures")
    void acceptsExtremeFiniteTemperatures() {
        assertEquals(-15.0,
                new WeatherReading(ID, BEACH_ID, SPEED, DIRECTION, -15.0, WATER_TEMPERATURE, FETCHED_AT)
                        .getTemperatureCelsius());
        assertEquals(-Double.MAX_VALUE,
                new WeatherReading(ID, BEACH_ID, SPEED, DIRECTION, -Double.MAX_VALUE, WATER_TEMPERATURE, FETCHED_AT)
                        .getTemperatureCelsius());
    }

    @Test
    @DisplayName("accepts negative and extreme finite water temperatures")
    void acceptsExtremeFiniteWaterTemperatures() {
        assertEquals(-2.0,
                new WeatherReading(ID, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, -2.0, FETCHED_AT)
                        .getWaterTemperatureCelsius());
        assertEquals(-Double.MAX_VALUE,
                new WeatherReading(ID, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, -Double.MAX_VALUE, FETCHED_AT)
                        .getWaterTemperatureCelsius());
    }

    // --- invariants ------------------------------------------------------

    @Test
    void rejectsNullId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(null, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT));
        assertEquals("Weather reading id cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullBeachId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(ID, null, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT));
        assertEquals("Weather reading beach id cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullWindSpeed() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(ID, BEACH_ID, null, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT));
        assertEquals("Weather reading wind speed cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullWindDirection() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(ID, BEACH_ID, SPEED, null, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT));
        assertEquals("Weather reading wind direction cannot be null!", ex.getMessage());
    }

    @ParameterizedTest(name = "rejects non-finite air temperature {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteTemperature(double temperature) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(ID, BEACH_ID, SPEED, DIRECTION, temperature, WATER_TEMPERATURE, FETCHED_AT));
        assertEquals("Weather reading temperature must be a finite number!", ex.getMessage());
    }

    @ParameterizedTest(name = "rejects non-finite water temperature {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteWaterTemperature(double waterTemperature) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(ID, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, waterTemperature, FETCHED_AT));
        assertEquals("Weather reading water temperature must be a finite number!", ex.getMessage());
    }

    @Test
    void rejectsNullFetchTime() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new WeatherReading(ID, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, null));
        assertEquals("Weather reading fetch time cannot be null!", ex.getMessage());
    }

    // --- identity-based equality -----------------------------------------

    @Test
    void equalsIsReflexive() {
        WeatherReading reading = rehydrated();
        assertEquals(reading, reading);
    }

    @Test
    void readingsWithSameIdAreEqualRegardlessOfState() {
        WeatherReading a = rehydrated();
        WeatherReading b = new WeatherReading(
                ID, BeachId.newId(), new WindSpeed(1.0), new WindDirection(1.0), 0.0, 0.0, FETCHED_AT.plusSeconds(1));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void readingsWithDifferentIdsAreNotEqual() {
        assertNotEquals(rehydrated(), new WeatherReading(
                WeatherReadingId.newId(), BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        WeatherReading reading = rehydrated();
        assertNotEquals(reading, null);
        assertNotEquals(reading, "reading");
    }

    // --- attribute-based sameAs ------------------------------------------

    @Test
    void sameAsIsTrueForIdenticalState() {
        assertTrue(rehydrated().sameAs(rehydrated()));
    }

    @Test
    void sameAsIsFalseForNull() {
        assertFalse(rehydrated().sameAs(null));
    }

    @Test
    void sameAsIsFalseWhenAnyAttributeDiffers() {
        assertFalse(rehydrated().sameAs(new WeatherReading(
                WeatherReadingId.newId(), BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT)));
        assertFalse(rehydrated().sameAs(new WeatherReading(
                ID, BeachId.newId(), SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT)));
        assertFalse(rehydrated().sameAs(new WeatherReading(
                ID, BEACH_ID, new WindSpeed(31.0), DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT)));
        assertFalse(rehydrated().sameAs(new WeatherReading(
                ID, BEACH_ID, SPEED, new WindDirection(10.0), TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT)));
        assertFalse(rehydrated().sameAs(new WeatherReading(
                ID, BEACH_ID, SPEED, DIRECTION, TEMPERATURE + 0.1, WATER_TEMPERATURE, FETCHED_AT)));
        assertFalse(rehydrated().sameAs(new WeatherReading(
                ID, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE + 0.1, FETCHED_AT)));
        assertFalse(rehydrated().sameAs(new WeatherReading(
                ID, BEACH_ID, SPEED, DIRECTION, TEMPERATURE, WATER_TEMPERATURE, FETCHED_AT.plusSeconds(1))));
    }

    // --- toString --------------------------------------------------------

    @Test
    void toStringContainsAllFields() {
        String rendered = rehydrated().toString();
        assertTrue(rendered.contains(ID.toString()));
        assertTrue(rendered.contains(BEACH_ID.toString()));
        assertTrue(rendered.contains("30.0 km/h"));
        assertTrue(rendered.contains("350.0°"));
        assertTrue(rendered.contains("21.5"));
        assertTrue(rendered.contains("18.5"));
        assertTrue(rendered.contains("2026-07-20T12:00:00Z"));
    }
}
