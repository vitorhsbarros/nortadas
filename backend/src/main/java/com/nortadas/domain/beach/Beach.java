package com.nortadas.domain.beach;

import com.nortadas.domain.region.Region;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.Name;

/**
 * A Portuguese coastal beach. Knows its own identity, name, geographic location
 * and the {@link Region} it belongs to.
 *
 * <p>The weather-reading history and the derivation of the current
 * {@link NortadaStatus} arrive with US009/US010 (see
 * {@code docs/OOA/class-responsibilities.md}); the Nortada grading rule itself
 * lives in a dedicated detection service, not here.
 *
 * <p>Equality is identity-based ({@link BeachId}), as for any domain entity:
 * two beaches are {@code equals} when they share an id, regardless of their other
 * attributes. To compare descriptive state as well, use {@link #sameAs(Beach)}.
 *
 * <p>Construction is factorized in {@link BeachFactory} (GoF Factory / GRASP
 * Creator): callers outside this package go through
 * {@code BeachFactory.create}/{@code rehydrate} rather than these constructors
 * directly.
 */
public class Beach {

    private final BeachId beachId;
    private final Name name;
    private final Latitude latitude;
    private final Longitude longitude;
    private final Region region;

    /** Creates a new beach, generating its own identity. */
    Beach(Name name, Latitude latitude, Longitude longitude, Region region) {
        this(BeachId.newId(), name, latitude, longitude, region);
    }

    /** Rehydrates a beach with a known identity (e.g. loaded from persistence). */
    Beach(BeachId beachId, Name name, Latitude latitude, Longitude longitude, Region region) {

        if (beachId == null) {
            throw new IllegalArgumentException("Beach id cannot be null!");
        }

        if (name == null) {
            throw new IllegalArgumentException("Beach name cannot be null!");
        }

        if (latitude == null) {
            throw new IllegalArgumentException("Beach latitude cannot be null!");
        }

        if (longitude == null) {
            throw new IllegalArgumentException("Beach longitude cannot be null!");
        }

        if (region == null) {
            throw new IllegalArgumentException("Beach region cannot be null!");
        }

        this.beachId = beachId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
    }

    public BeachId getBeachId() {
        return beachId;
    }

    public Name getName() {
        return name;
    }

    public Latitude getLatitude() {
        return latitude;
    }

    public Longitude getLongitude() {
        return longitude;
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Beach)) {
            return false;
        }
        Beach that = (Beach) other;
        return beachId.equals(that.beachId);
    }

    @Override
    public int hashCode() {
        return beachId.hashCode();
    }

    /**
     * Attribute-based comparison: {@code true} only when {@code other} is a beach
     * with the same identity <em>and</em> the same name, latitude, longitude and
     * region (i.e. identical state). The region is compared by its own identity
     * ({@link Region#equals(Object)}) — two beaches are {@code sameAs} only when
     * they reference the same region entity.
     *
     * <p>Contrast with {@link #equals(Object)}: two beaches with the same id but
     * different attributes are {@code equals} (the same entity) yet not
     * {@code sameAs} (different state); beaches with different ids are neither.
     * Null-safe — returns {@code false} for a {@code null} argument.
     */
    public boolean sameAs(Beach other) {
        if (other == null) {
            return false;
        }
        return beachId.equals(other.beachId)
                && name.equals(other.name)
                && latitude.equals(other.latitude)
                && longitude.equals(other.longitude)
                && region.equals(other.region);
    }

    @Override
    public String toString() {
        return "Beach{beachId=" + beachId + ", name=" + name
                + ", latitude=" + latitude + ", longitude=" + longitude
                + ", region=" + region + "}";
    }
}
