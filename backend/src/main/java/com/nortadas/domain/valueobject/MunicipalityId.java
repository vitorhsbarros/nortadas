package com.nortadas.domain.valueobject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identity of a {@link com.nortadas.domain.municipality.Municipality}: Portugal's
 * official INE/DICOFRE municipality code — exactly four digits, e.g. {@code 0107}
 * for Espinho.
 *
 * <p>Unlike {@link RegionId} (derived from the region's name) or {@link BeachId}
 * (randomly generated), a municipality's identity is always an externally-known
 * real-world code assigned outside this system — there is no "generate a fresh
 * one" case. The value is stored as a {@code String}, not a numeric type: several
 * real codes have significant leading zeros (e.g. {@code 0107}), which a numeric
 * representation would silently drop, corrupting the identifier.
 *
 * <p>Rehydrate an identity from its string form (e.g. loaded from persistence or
 * received from a client) via {@link #of(String)}, which validates the
 * four-digit format.
 */
public final class MunicipalityId {

    /** Exactly four digits. */
    private static final Pattern FORMAT = Pattern.compile("^[0-9]{4}$");

    private final String value;

    private MunicipalityId(String value) {
        this.value = value;
    }

    /**
     * Rehydrates an identity from its full string form (e.g. loaded from
     * persistence), validating the four-digit format.
     */
    public static MunicipalityId of(String value) {

        if (value == null) {
            throw new IllegalArgumentException("Municipality id cannot be null!");
        }

        Matcher matcher = FORMAT.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Municipality id must be exactly 4 digits, but was: " + value);
        }

        return new MunicipalityId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MunicipalityId)) {
            return false;
        }
        MunicipalityId that = (MunicipalityId) other;
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
