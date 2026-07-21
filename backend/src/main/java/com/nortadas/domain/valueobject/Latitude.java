package com.nortadas.domain.valueobject;

/**
 * A geographic latitude in decimal degrees.
 *
 * <p>Invariant: a finite value between -90 and 90 inclusive.
 */
public final class Latitude {

    private final double degrees;

    /**
     * {@code -0.0} is normalized to {@code 0.0} so {@code equals}/{@code hashCode}
     * are consistent for zero.
     */
    public Latitude(double degrees) {

        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("Latitude must be a finite number!");
        }

        if (degrees < -90.0 || degrees > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees!");
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
        if (!(other instanceof Latitude)) {
            return false;
        }
        Latitude that = (Latitude) other;
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
