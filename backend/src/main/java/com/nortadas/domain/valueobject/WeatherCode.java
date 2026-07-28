package com.nortadas.domain.valueobject;

/**
 * A raw WMO weather-interpretation code (the {@code ww} table Open-Meteo returns
 * as {@code weather_code}) — the source-of-truth weather observation on a
 * {@link com.nortadas.domain.weatherreading.WeatherReading}, from which the
 * client-facing {@link WeatherCondition} category is derived.
 *
 * <p>Invariant: an integer in the WMO {@code ww} range {@code [0, 99]}.
 */
public final class WeatherCode {

    private final int value;

    public WeatherCode(int value) {

        if (value < 0 || value > 99) {
            throw new IllegalArgumentException("Weather code must be a WMO code between 0 and 99!");
        }

        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WeatherCode)) {
            return false;
        }
        WeatherCode that = (WeatherCode) other;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return "WeatherCode{" + value + "}";
    }
}
