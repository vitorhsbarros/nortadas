package com.nortadas.domain.valueobject;

/**
 * A wind direction in meteorological degrees (0° = North, clockwise).
 *
 * <p>Invariant: a finite value in the half-open range [0, 360). North may be
 * expressed only as 0°, never 360°, so every direction has a single representation.
 */
public final class WindDirection {

    private final double degrees;

    /**
     * {@code -0.0} is normalized to {@code 0.0} so {@code equals}/{@code hashCode}
     * are consistent for zero.
     */
    public WindDirection(double degrees) {

        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("Wind direction must be a finite number!");
        }

        if (degrees < 0.0 || degrees >= 360.0) {
            throw new IllegalArgumentException("Wind direction must be between 0 (inclusive) and 360 (exclusive) degrees!");
        }

        if (degrees == 0.0) {
            degrees = 0.0;
        }

        this.degrees = degrees;
    }

    public double getDegrees() {
        return degrees;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WindDirection)) {
            return false;
        }
        WindDirection that = (WindDirection) other;
        return Double.compare(degrees, that.degrees) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(degrees);
    }

    @Override
    public String toString() {
        return degrees + "°";
    }
}
