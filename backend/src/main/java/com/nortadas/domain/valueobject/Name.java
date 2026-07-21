package com.nortadas.domain.valueobject;

/**
 * A validated human-readable name (beach name, region name, ...).
 *
 * <p>Invariants, enforced at construction: not blank, between 2 and 80 characters,
 * letters (including accented), spaces, apostrophes and hyphens only.
 */
public final class Name {

    private final String value;

    public Name(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank!");
        }

        if (value.length() < 2 || value.length() > 80) {
            throw new IllegalArgumentException("Name must have between 2 and 80 characters!");
        }

        if (!value.matches("^[a-zA-ZÀ-ÿ\\s'-]+$")) {
            throw new IllegalArgumentException("Name cannot contain special characters!");
        }

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Name)) {
            return false;
        }
        Name that = (Name) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
