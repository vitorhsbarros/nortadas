package com.nortadas.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class WeatherConditionTest {

    /**
     * Every WMO code that maps to a recognised category. Each individual case of
     * {@link WeatherCondition#fromWmoCode} is exercised so the mapping's branch
     * coverage is complete.
     */
    @ParameterizedTest(name = "code {0} -> {1}")
    @CsvSource({
            "0, CLEAR",
            "1, CLOUDY",
            "2, CLOUDY",
            "3, CLOUDY",
            "45, FOG",
            "48, FOG",
            "51, DRIZZLE",
            "53, DRIZZLE",
            "55, DRIZZLE",
            "56, DRIZZLE",
            "57, DRIZZLE",
            "61, RAIN",
            "63, RAIN",
            "65, RAIN",
            "66, RAIN",
            "67, RAIN",
            "80, RAIN",
            "81, RAIN",
            "82, RAIN",
            "71, SNOW",
            "73, SNOW",
            "75, SNOW",
            "77, SNOW",
            "85, SNOW",
            "86, SNOW",
            "95, THUNDERSTORM",
            "96, THUNDERSTORM",
            "99, THUNDERSTORM"
    })
    void mapsEachRecognisedCodeToItsCategory(int code, WeatherCondition expected) {
        assertEquals(expected, WeatherCondition.fromWmoCode(new WeatherCode(code)));
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 44, 50, 60, 90, 98})
    @DisplayName("unrecognised but valid codes fall back to UNKNOWN")
    void mapsUnrecognisedCodesToUnknown(int code) {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(new WeatherCode(code)));
    }

    @Test
    void rejectsNullCode() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> WeatherCondition.fromWmoCode(null));
        assertEquals("Weather code cannot be null!", ex.getMessage());
    }
}
