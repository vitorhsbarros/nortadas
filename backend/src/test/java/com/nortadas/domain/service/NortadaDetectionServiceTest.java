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

class NortadaDetectionServiceTest {

    private static final BeachId BEACH_ID = BeachId.newId();
    private static final Instant FETCHED_AT = Instant.parse("2026-07-20T12:00:00Z");

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

    @Test
    @DisplayName("applies the US010 sector+speed rule when given SectorSpeedDetectionStrategy")
    void appliesUs010RuleWithSectorSpeedStrategy() {
        NortadaDetectionService service =
                new NortadaDetectionService(new SectorSpeedDetectionStrategy());

        assertEquals(NortadaStatus.SEVERE, service.detect(reading(350.0, 60.0)));
        assertEquals(NortadaStatus.NONE, service.detect(reading(180.0, 60.0)));
    }

    @Test
    @DisplayName("delegates to the injected strategy")
    void delegatesToInjectedStrategy() {
        NortadaDetectionService service =
                new NortadaDetectionService(r -> NortadaStatus.LIGHT);

        // The stub ignores the rule, proving delegation: an off-sector reading still returns LIGHT.
        assertEquals(NortadaStatus.LIGHT, service.detect(reading(180.0, 5.0)));
    }

    @Test
    @DisplayName("null strategy is rejected")
    void nullStrategyRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new NortadaDetectionService(null));
    }
}
