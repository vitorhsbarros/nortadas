package com.nortadas.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WeatherCodeTest {

    private static final String OUT_OF_RANGE_MESSAGE = "Weather code must be a WMO code between 0 and 99!";

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 45, 61, 95, 99})
    @DisplayName("accepts any WMO code in [0, 99] and exposes it")
    void acceptsCodesInRange(int code) {
        assertEquals(code, new WeatherCode(code).getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 100, 1000})
    @DisplayName("rejects codes outside [0, 99]")
    void rejectsCodesOutOfRange(int code) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new WeatherCode(code));
        assertEquals(OUT_OF_RANGE_MESSAGE, ex.getMessage());
    }

    @Test
    void equalsIsReflexive() {
        WeatherCode code = new WeatherCode(3);
        assertEquals(code, code);
    }

    @Test
    void codesWithSameValueAreEqualWithMatchingHashCodes() {
        assertEquals(new WeatherCode(48), new WeatherCode(48));
        assertEquals(new WeatherCode(48).hashCode(), new WeatherCode(48).hashCode());
    }

    @Test
    void codesWithDifferentValuesAreNotEqual() {
        assertNotEquals(new WeatherCode(0), new WeatherCode(1));
    }

    @Test
    @DisplayName("is not equal to null or another type")
    void isNotEqualToNullOrOtherType() {
        WeatherCode code = new WeatherCode(3);
        // WeatherCode first, so its own equals(...) is the one invoked.
        assertNotEquals(code, null);
        assertNotEquals(code, 3);
    }

    @Test
    void toStringRendersTheValue() {
        assertEquals("WeatherCode{3}", new WeatherCode(3).toString());
    }
}
