package com.nortadas.domain.valueobject;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identity of a {@link com.nortadas.domain.region.Region}: a short natural code
 * derived from the region's name — e.g. {@code NOR} for "Norte". Regions are a
 * fixed, curated vocabulary (Portugal's coastal regions), so — unlike
 * {@link BeachId} — identity here is a deterministic natural key rather than a
 * randomly generated one: the same name always derives the same code, and
 * uniqueness comes from the small, closed set of region names rather than from
 * the id itself.
 *
 * <p>Code derivation: the name is Unicode-normalized with accents/diacritics
 * stripped, non-letters dropped, uppercased, and the first three letters taken
 * (fewer if the name yields fewer letters — minimum one). "Açores" → {@code ACO},
 * "Norte" → {@code NOR}.
 *
 * <p>The code is a <strong>snapshot at creation</strong>: renaming a region does
 * NOT change its id — identity is immutable and the code may therefore lag
 * behind the current name.
 *
 * <p>The domain generates its own identities (GRASP Creator) rather than
 * delegating to the persistence layer.
 */
public final class RegionId {

    /** 1–3 uppercase letters. */
    private static final Pattern FORMAT = Pattern.compile("^[A-Z]{1,3}$");

    private final String value;

    private RegionId(String value) {
        this.value = value;
    }

    /**
     * Derives the identity for a region from its name (GRASP Creator —
     * {@code Region} passes its own name). Deterministic: the same name always
     * yields the same code.
     */
    public static RegionId fromName(Name regionName) {

        if (regionName == null) {
            throw new IllegalArgumentException("Region id requires a region name!");
        }

        return new RegionId(derivePrefix(regionName.getValue()));
    }

    /**
     * Rehydrates an identity from its full string form (e.g. loaded from
     * persistence), validating the 1–3 uppercase letter format.
     */
    public static RegionId of(String value) {

        if (value == null) {
            throw new IllegalArgumentException("Region id cannot be null!");
        }

        Matcher matcher = FORMAT.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Region id must be 1-3 uppercase letters, but was: " + value);
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
