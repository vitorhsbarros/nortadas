package com.nortadas.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.valueobject.WeatherCode;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import com.nortadas.domain.weatherreading.WeatherReading;
import com.nortadas.domain.weatherreading.WeatherReadingFactory;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SectorSpeedDetectionStrategyTest {

    private static final BeachId BEACH_ID = BeachId.newId();
    private static final Instant FETCHED_AT = Instant.parse("2026-07-20T12:00:00Z");

    private final SectorSpeedDetectionStrategy strategy = new SectorSpeedDetectionStrategy();

    private static WeatherReading reading(double degrees, double kmPerHour) {
        return WeatherReadingFactory.create(
                BEACH_ID,
                new WindSpeed(kmPerHour),
                new WindDirection(degrees),
                21.5,
                18.5,
                new WeatherCode(3),
                FETCHED_AT);
    }

    @ParameterizedTest(name = "{0}° off-sector with speed {1} km/h -> NONE")
    @CsvSource({
            // Just outside each edge, plus clearly off-sector bearings — always NONE regardless of speed.
            "314.999, 100.0",
            "45.001, 100.0",
            "46.0, 60.0",
            "90.0, 30.0",
            "180.0, 80.0",
            "270.0, 55.0",
            "314.0, 55.0"
    })
    @DisplayName("wind outside the 315–45 sector is always NONE, whatever the speed")
    void offSectorIsAlwaysNone(double degrees, double kmPerHour) {
        assertEquals(NortadaStatus.NONE, strategy.detect(reading(degrees, kmPerHour)));
    }

    @ParameterizedTest(name = "{0}° is in-sector (graded, not gated out)")
    @CsvSource({
            "315.0",   // lower edge, inclusive
            "45.0",    // upper edge, inclusive
            "0.0",     // due North, wrap point
            "359.999", // just below 360, in-sector
            "350.0",
            "10.0",
            "44.999"
    })
    @DisplayName("315 and 45 are inclusive and the sector wraps through 0")
    void inSectorEdgesAreGradedNotGatedOut(double degrees) {
        // A gradeable speed (30 km/h -> MODERATE) proves the reading passed the gate.
        assertEquals(NortadaStatus.MODERATE, strategy.detect(reading(degrees, 30.0)));
    }

    @ParameterizedTest(name = "in-sector at {0} km/h -> {1}")
    @CsvSource({
            "0.0, NONE",
            "14.999, NONE",
            "15.0, LIGHT",
            "24.999, LIGHT",
            "25.0, MODERATE",
            "39.999, MODERATE",
            "40.0, STRONG",
            "54.999, STRONG",
            "55.0, SEVERE",
            "80.0, SEVERE"
    })
    @DisplayName("within the sector, sustained speed grades into the five levels at the exact boundaries")
    void speedGradesWithinSector(double kmPerHour, NortadaStatus expected) {
        // 350° is comfortably in-sector, so only speed decides the grade.
        assertEquals(expected, strategy.detect(reading(350.0, kmPerHour)));
    }

    @Test
    @DisplayName("null reading is rejected")
    void nullReadingRejected() {
        assertThrows(IllegalArgumentException.class, () -> strategy.detect(null));
    }
}
