package com.nortadas.domain.valueobject;

import java.util.regex.Pattern;

/**
 * A validated human-readable name (beach name, region name, ...).
 *
 * <p>Invariants, enforced at construction: not blank, between 2 and 80 characters,
 * composed only of Unicode letters (including accented, e.g. "Açores", "São"),
 * spaces, apostrophes and hyphens, and containing at least one actual letter (so
 * letter-less values such as "--" or "''" are rejected).
 *
 * <p>The character check uses the Unicode {@code IsAlphabetic} property rather
 * than a Latin-1 code-point range: the old {@code À-ÿ} range wrongly admitted the
 * multiplication ({@code ×}, U+00D7) and division ({@code ÷}, U+00F7) signs, which
 * fall inside it but are math symbols, not letters.
 */
public final class Name {

    /** Every character must be a Unicode letter, whitespace, apostrophe or hyphen. */
    private static final Pattern ALLOWED_CHARACTERS =
            Pattern.compile("[\\p{IsAlphabetic}\\s'-]+");

    /** At least one character must be an actual Unicode letter. */
    private static final Pattern CONTAINS_LETTER =
            Pattern.compile("\\p{IsAlphabetic}");

    private final String value;

    public Name(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank!");
        }

        if (value.length() < 2 || value.length() > 80) {
            throw new IllegalArgumentException("Name must have between 2 and 80 characters!");
        }

        if (!ALLOWED_CHARACTERS.matcher(value).matches()) {
            throw new IllegalArgumentException("Name cannot contain special characters!");
        }

        if (!CONTAINS_LETTER.matcher(value).find()) {
            throw new IllegalArgumentException("Name must contain at least one letter!");
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
