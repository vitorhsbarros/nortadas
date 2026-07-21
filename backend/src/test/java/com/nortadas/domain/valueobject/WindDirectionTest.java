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

class WindDirectionTest {

    // --- half-open range [0, 360) ----------------------------------------

    static DoubleStream inRangeDegrees() {
        return DoubleStream.of(
                0.0,                   // exact minimum (North), inclusive
                Math.nextUp(0.0),      // just inside minimum
                337.5,                 // NNW — Nortada sector edge
                359.9,
                Math.nextDown(360.0)   // just inside the exclusive maximum
        );
    }

    @ParameterizedTest(name = "accepts {0} degrees")
    @MethodSource("inRangeDegrees")
    void acceptsInRangeDegrees(double degrees) {
        assertEquals(degrees, new WindDirection(degrees).getDegrees());
    }

    static DoubleStream outOfRangeDegrees() {
        return DoubleStream.of(
                Math.nextDown(0.0),    // just below minimum
                -0.1,
                -360.0,
                360.0,                 // exact maximum is EXCLUDED — North is only 0
                Math.nextUp(360.0),
                720.0
        );
    }

    @ParameterizedTest(name = "rejects {0} degrees")
    @MethodSource("outOfRangeDegrees")
    void rejectsOutOfRangeDegrees(double degrees) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new WindDirection(degrees));
        assertEquals("Wind direction must be between 0 (inclusive) and 360 (exclusive) degrees!",
                ex.getMessage());
    }

    // --- finiteness ------------------------------------------------------

    @ParameterizedTest(name = "rejects non-finite {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteValues(double degrees) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new WindDirection(degrees));
        assertEquals("Wind direction must be a finite number!", ex.getMessage());
    }

    // --- equals / hashCode / toString ------------------------------------

    @Test
    void equalsIsReflexive() {
        WindDirection direction = new WindDirection(350.0);
        assertEquals(direction, direction);
    }

    @Test
    void directionsWithSameDegreesAreEqualWithMatchingHashCodes() {
        assertEquals(new WindDirection(350.0), new WindDirection(350.0));
        assertEquals(new WindDirection(350.0).hashCode(), new WindDirection(350.0).hashCode());
    }

    @Test
    void directionsWithDifferentDegreesAreNotEqual() {
        assertNotEquals(new WindDirection(350.0), new WindDirection(170.0));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        WindDirection direction = new WindDirection(350.0);
        assertNotEquals(direction, null);
        assertNotEquals(direction, 350.0);
    }

    @Test
    @DisplayName("toString renders degrees with the degree sign")
    void toStringRendersDegreeSign() {
        assertEquals("350.0°", new WindDirection(350.0).toString());
    }

    @Test
    @DisplayName("-0.0 is normalized to 0.0 so equals/hashCode are consistent for zero")
    void negativeZeroEqualsPositiveZero() {
        WindDirection positiveZero = new WindDirection(0.0);
        WindDirection negativeZero = new WindDirection(-0.0);

        assertEquals(positiveZero, negativeZero);
        assertEquals(positiveZero.hashCode(), negativeZero.hashCode());

        // assertEquals on doubles treats -0.0 == 0.0, so use Double.compare for a
        // bit-exact check that the stored value is actually positive zero.
        assertTrue(Double.compare(negativeZero.getDegrees(), 0.0) == 0);
    }
}
