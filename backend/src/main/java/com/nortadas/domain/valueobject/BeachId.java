package com.nortadas.domain.valueobject;

import java.util.UUID;

/**
 * Identity of a {@link com.nortadas.domain.Beach}. The domain generates its own
 * identities (GRASP Creator) rather than delegating to the persistence layer.
 */
public final class BeachId {

    private final UUID value;

    public BeachId(UUID value) {

        if (value == null) {
            throw new IllegalArgumentException("Beach id cannot be null!");
        }

        this.value = value;
    }

    /** Generates a fresh, random identity for a newly created beach. */
    public static BeachId newId() {
        return new BeachId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BeachId)) {
            return false;
        }
        BeachId that = (BeachId) other;
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
