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

class LatitudeTest {

    // --- range boundaries ------------------------------------------------

    static DoubleStream inRangeDegrees() {
        return DoubleStream.of(
                -90.0,                 // exact minimum
                Math.nextUp(-90.0),    // just inside minimum
                0.0,
                40.6405,               // Praia da Barra
                Math.nextDown(90.0),   // just inside maximum
                90.0                   // exact maximum
        );
    }

    @ParameterizedTest(name = "accepts {0} degrees")
    @MethodSource("inRangeDegrees")
    void acceptsInRangeDegrees(double degrees) {
        assertEquals(degrees, new Latitude(degrees).getDegrees());
    }

    static DoubleStream outOfRangeDegrees() {
        return DoubleStream.of(
                Math.nextDown(-90.0),  // just below minimum
                Math.nextUp(90.0),     // just above maximum
                -90.1,
                90.1,
                -1000.0,
                1000.0
        );
    }

    @ParameterizedTest(name = "rejects {0} degrees")
    @MethodSource("outOfRangeDegrees")
    void rejectsOutOfRangeDegrees(double degrees) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new Latitude(degrees));
        assertEquals("Latitude must be between -90 and 90 degrees!", ex.getMessage());
    }

    // --- finiteness ------------------------------------------------------

    @ParameterizedTest(name = "rejects non-finite {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteValues(double degrees) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new Latitude(degrees));
        assertEquals("Latitude must be a finite number!", ex.getMessage());
    }

    // --- equals / hashCode / toString ------------------------------------

    @Test
    void equalsIsReflexive() {
        Latitude latitude = new Latitude(40.5);
        assertEquals(latitude, latitude);
    }

    @Test
    void latitudesWithSameDegreesAreEqualWithMatchingHashCodes() {
        assertEquals(new Latitude(40.5), new Latitude(40.5));
        assertEquals(new Latitude(40.5).hashCode(), new Latitude(40.5).hashCode());
    }

    @Test
    void latitudesWithDifferentDegreesAreNotEqual() {
        assertNotEquals(new Latitude(40.5), new Latitude(-40.5));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        Latitude latitude = new Latitude(40.5);
        assertNotEquals(latitude, null);
        assertNotEquals(latitude, 40.5);
    }

    @Test
    @DisplayName("toString renders degrees with the degree sign")
    void toStringRendersDegreeSign() {
        assertEquals("40.5°", new Latitude(40.5).toString());
    }
}
