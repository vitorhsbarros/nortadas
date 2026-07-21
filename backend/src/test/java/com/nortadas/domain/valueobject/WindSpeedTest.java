package com.nortadas.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindSpeedTest {

    // --- range boundary (>= 0) -------------------------------------------

    static DoubleStream validSpeeds() {
        return DoubleStream.of(
                0.0,               // exact minimum
                Double.MIN_VALUE,  // just above minimum
                15.0,              // US010 threshold values are plausible inputs
                25.0,
                40.0,
                55.0,
                180.0
        );
    }

    @ParameterizedTest(name = "accepts {0} km/h")
    @MethodSource("validSpeeds")
    void acceptsNonNegativeSpeeds(double kmPerHour) {
        assertEquals(kmPerHour, new WindSpeed(kmPerHour).getKmPerHour());
    }

    @ParameterizedTest(name = "rejects negative speed {0}")
    @ValueSource(doubles = {-4.9E-324 /* just below zero */, -0.1, -55.0})
    void rejectsNegativeSpeeds(double kmPerHour) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new WindSpeed(kmPerHour));
        assertEquals("Wind speed cannot be negative!", ex.getMessage());
    }

    // --- finiteness ------------------------------------------------------

    @ParameterizedTest(name = "rejects non-finite {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteValues(double kmPerHour) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new WindSpeed(kmPerHour));
        assertEquals("Wind speed must be a finite number!", ex.getMessage());
    }

    // --- equals / hashCode / toString ------------------------------------

    @Test
    void equalsIsReflexive() {
        WindSpeed speed = new WindSpeed(25.0);
        assertEquals(speed, speed);
    }

    @Test
    void speedsWithSameValueAreEqualWithMatchingHashCodes() {
        assertEquals(new WindSpeed(25.0), new WindSpeed(25.0));
        assertEquals(new WindSpeed(25.0).hashCode(), new WindSpeed(25.0).hashCode());
    }

    @Test
    void speedsWithDifferentValuesAreNotEqual() {
        assertNotEquals(new WindSpeed(25.0), new WindSpeed(40.0));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        WindSpeed speed = new WindSpeed(25.0);
        assertNotEquals(speed, null);
        assertNotEquals(speed, 25.0);
    }

    @Test
    @DisplayName("toString renders the value with its km/h unit")
    void toStringRendersUnit() {
        assertEquals("25.0 km/h", new WindSpeed(25.0).toString());
    }

    @Test
    @DisplayName("-0.0 is normalized to 0.0 so equals/hashCode are consistent for zero")
    void negativeZeroEqualsPositiveZero() {
        WindSpeed positiveZero = new WindSpeed(0.0);
        WindSpeed negativeZero = new WindSpeed(-0.0);

        assertEquals(positiveZero, negativeZero);
        assertEquals(positiveZero.hashCode(), negativeZero.hashCode());

        // assertEquals on doubles treats -0.0 == 0.0, so use Double.compare for a
        // bit-exact check that the stored value is actually positive zero.
        assertTrue(Double.compare(negativeZero.getKmPerHour(), 0.0) == 0);
    }
}
