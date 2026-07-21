package com.nortadas.domain.valueobject;

/**
 * A sustained wind speed in km/h (the unit US010's Nortada grading thresholds use).
 *
 * <p>Invariant: a finite, non-negative value.
 */
public final class WindSpeed {

    private final double kmPerHour;

    /**
     * {@code -0.0} is normalized to {@code 0.0} so {@code equals}/{@code hashCode}
     * are consistent for zero.
     */
    public WindSpeed(double kmPerHour) {

        if (!Double.isFinite(kmPerHour)) {
            throw new IllegalArgumentException("Wind speed must be a finite number!");
        }

        if (kmPerHour < 0.0) {
            throw new IllegalArgumentException("Wind speed cannot be negative!");
        }

        if (kmPerHour == 0.0) {
            kmPerHour = 0.0;
        }

        this.kmPerHour = kmPerHour;
    }

    public double getKmPerHour() {
        return kmPerHour;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WindSpeed)) {
            return false;
        }
        WindSpeed that = (WindSpeed) other;
        return Double.compare(kmPerHour, that.kmPerHour) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(kmPerHour);
    }

    @Override
    public String toString() {
        return kmPerHour + " km/h";
    }
}
