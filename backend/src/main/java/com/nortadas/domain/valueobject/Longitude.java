package com.nortadas.domain.valueobject;

/**
 * A geographic longitude in decimal degrees.
 *
 * <p>Invariant: a finite value between -180 and 180 inclusive.
 */
public final class Longitude {

    private final double degrees;

    /**
     * {@code -0.0} is normalized to {@code 0.0} so {@code equals}/{@code hashCode}
     * are consistent for zero.
     */
    public Longitude(double degrees) {

        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("Longitude must be a finite number!");
        }

        if (degrees < -180.0 || degrees > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees!");
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
        if (!(other instanceof Longitude)) {
            return false;
        }
        Longitude that = (Longitude) other;
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
