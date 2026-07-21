package com.nortadas.domain.valueobject;

import java.util.UUID;

/**
 * Identity of a {@link com.nortadas.domain.weatherreading.WeatherReading}. The
 * domain generates its own identities (GRASP Creator) rather than delegating to
 * the persistence layer. Unlike {@code RegionId}/{@code MunicipalityId} (natural
 * keys), a weather-reading id is a randomly generated surrogate, like
 * {@code BeachId} — a weather reading has no natural key of its own.
 */
public final class WeatherReadingId {

    private final UUID value;

    public WeatherReadingId(UUID value) {

        if (value == null) {
            throw new IllegalArgumentException("Weather reading id cannot be null!");
        }

        this.value = value;
    }

    /** Generates a fresh, random identity for a newly fetched weather reading. */
    public static WeatherReadingId newId() {
        return new WeatherReadingId(UUID.randomUUID());
    }

    /** Rehydrates an identity from its canonical string form (e.g. from persistence). */
    public static WeatherReadingId of(String value) {

        if (value == null) {
            throw new IllegalArgumentException("Weather reading id cannot be null!");
        }

        try {
            return new WeatherReadingId(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Weather reading id must be a valid UUID!");
        }
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WeatherReadingId)) {
            return false;
        }
        WeatherReadingId that = (WeatherReadingId) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
