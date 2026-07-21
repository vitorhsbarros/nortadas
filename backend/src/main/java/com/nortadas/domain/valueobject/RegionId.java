package com.nortadas.domain.valueobject;

import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identity of a {@link com.nortadas.domain.region.Region}, in the form
 * {@code <PREFIX>-<uuid>} where the prefix is derived from the region's name at
 * creation time — e.g. {@code NOR-550e8400-e29b-41d4-a716-446655440000} for
 * "Norte".
 *
 * <p>Prefix derivation: the name is Unicode-normalized with accents/diacritics
 * stripped, non-letters dropped, uppercased, and the first three letters taken
 * (fewer if the name yields fewer letters — minimum one). "Açores" → {@code ACO},
 * "Norte" → {@code NOR}.
 *
 * <p>The prefix is a <strong>snapshot at creation</strong>: renaming a region
 * does NOT change its id — identity is immutable and the embedded prefix may
 * therefore lag behind the current name.
 *
 * <p>The domain generates its own identities (GRASP Creator) rather than
 * delegating to the persistence layer.
 */
public final class RegionId {

    /** 1–3 uppercase letters, a hyphen, then a canonical lower-case UUID. */
    private static final Pattern FORMAT =
            Pattern.compile("^[A-Z]{1,3}-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private final String value;

    private RegionId(String value) {
        this.value = value;
    }

    /**
     * Generates a fresh identity for a newly created region, deriving the prefix
     * from the region's name (GRASP Creator — {@code Region} passes its own name).
     */
    public static RegionId newId(Name regionName) {

        if (regionName == null) {
            throw new IllegalArgumentException("Region id requires a region name!");
        }

        return new RegionId(derivePrefix(regionName.getValue()) + "-" + UUID.randomUUID());
    }

    /**
     * Rehydrates an identity from its full string form (e.g. loaded from
     * persistence), validating the {@code <PREFIX>-<uuid>} format.
     */
    public static RegionId of(String value) {

        if (value == null) {
            throw new IllegalArgumentException("Region id cannot be null!");
        }

        Matcher matcher = FORMAT.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Region id must be 1-3 uppercase letters, a hyphen, and a canonical UUID, but was: " + value);
        }

        return new RegionId(value);
    }

    private static String derivePrefix(String name) {

        // Name guarantees at least one Unicode letter, so `letters` is never empty here.
        String withoutDiacritics = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        StringBuilder letters = new StringBuilder(3);
        for (int i = 0; i < withoutDiacritics.length() && letters.length() < 3; i++) {
            char c = withoutDiacritics.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }

        return letters.toString();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RegionId)) {
            return false;
        }
        RegionId that = (RegionId) other;
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
