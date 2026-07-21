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

class LongitudeTest {

    // --- range boundaries ------------------------------------------------

    static DoubleStream inRangeDegrees() {
        return DoubleStream.of(
                -180.0,                // exact minimum
                Math.nextUp(-180.0),   // just inside minimum
                0.0,
                -8.7527,               // Praia da Barra
                Math.nextDown(180.0),  // just inside maximum
                180.0                  // exact maximum
        );
    }

    @ParameterizedTest(name = "accepts {0} degrees")
    @MethodSource("inRangeDegrees")
    void acceptsInRangeDegrees(double degrees) {
        assertEquals(degrees, new Longitude(degrees).getDegrees());
    }

    static DoubleStream outOfRangeDegrees() {
        return DoubleStream.of(
                Math.nextDown(-180.0), // just below minimum
                Math.nextUp(180.0),    // just above maximum
                -180.1,
                180.1,
                -1000.0,
                1000.0
        );
    }

    @ParameterizedTest(name = "rejects {0} degrees")
    @MethodSource("outOfRangeDegrees")
    void rejectsOutOfRangeDegrees(double degrees) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new Longitude(degrees));
        assertEquals("Longitude must be between -180 and 180 degrees!", ex.getMessage());
    }

    // --- finiteness ------------------------------------------------------

    @ParameterizedTest(name = "rejects non-finite {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteValues(double degrees) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new Longitude(degrees));
        assertEquals("Longitude must be a finite number!", ex.getMessage());
    }

    // --- equals / hashCode / toString ------------------------------------

    @Test
    void equalsIsReflexive() {
        Longitude longitude = new Longitude(-8.75);
        assertEquals(longitude, longitude);
    }

    @Test
    void longitudesWithSameDegreesAreEqualWithMatchingHashCodes() {
        assertEquals(new Longitude(-8.75), new Longitude(-8.75));
        assertEquals(new Longitude(-8.75).hashCode(), new Longitude(-8.75).hashCode());
    }

    @Test
    void longitudesWithDifferentDegreesAreNotEqual() {
        assertNotEquals(new Longitude(-8.75), new Longitude(8.75));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        Longitude longitude = new Longitude(-8.75);
        assertNotEquals(longitude, null);
        assertNotEquals(longitude, -8.75);
    }

    @Test
    @DisplayName("toString renders degrees with the degree sign")
    void toStringRendersDegreeSign() {
        assertEquals("-8.75°", new Longitude(-8.75).toString());
    }

    @Test
    @DisplayName("-0.0 is normalized to 0.0 so equals/hashCode are consistent for zero")
    void negativeZeroEqualsPositiveZero() {
        Longitude positiveZero = new Longitude(0.0);
        Longitude negativeZero = new Longitude(-0.0);

        assertEquals(positiveZero, negativeZero);
        assertEquals(positiveZero.hashCode(), negativeZero.hashCode());

        // assertEquals on doubles treats -0.0 == 0.0, so use Double.compare for a
        // bit-exact check that the stored value is actually positive zero.
        assertTrue(Double.compare(negativeZero.getDegrees(), 0.0) == 0);
    }
}
