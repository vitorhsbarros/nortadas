package com.nortadas.domain.region;

import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.RegionId;

/**
 * A coastal region grouping the beaches located within it (e.g. Norte, Centro,
 * Lisboa, Alentejo, Algarve). Knows its own identity and name; weather and
 * Nortada status are a {@link com.nortadas.domain.beach.Beach}/
 * {@link com.nortadas.domain.valueobject.WeatherReading} concern.
 *
 * <p>Equality is identity-based ({@link RegionId}), as for any domain entity:
 * two regions are {@code equals} when they share an id, regardless of name. To
 * compare descriptive state as well, use {@link #sameAs(Region)}.
 */
public class Region {

    private final RegionId regionId;
    private final Name name;

    /**
     * Creates a new region, deriving its own identity from its name (GRASP
     * Creator). The derivation is a snapshot at creation: renaming a region
     * later does not change its identity.
     */
    public Region(Name name) {
        this(RegionId.fromName(name), name);
    }

    /** Rehydrates a region with a known identity (e.g. loaded from persistence). */
    public Region(RegionId regionId, Name name) {

        if (regionId == null) {
            throw new IllegalArgumentException("Region id cannot be null!");
        }

        if (name == null) {
            throw new IllegalArgumentException("Region name cannot be null!");
        }

        this.regionId = regionId;
        this.name = name;
    }

    public RegionId getRegionId() {
        return regionId;
    }

    public Name getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Region)) {
            return false;
        }
        Region that = (Region) other;
        return regionId.equals(that.regionId);
    }

    @Override
    public int hashCode() {
        return regionId.hashCode();
    }

    /**
     * Attribute-based comparison: {@code true} only when {@code other} is a region
     * with the same identity <em>and</em> the same name (i.e. identical state).
     *
     * <p>Contrast with {@link #equals(Object)}: two regions with the same id but
     * different names are {@code equals} (the same entity) yet not {@code sameAs}
     * (different state); regions with different ids are neither. Null-safe —
     * returns {@code false} for a {@code null} argument.
     */
    public boolean sameAs(Region other) {
        if (other == null) {
            return false;
        }
        return regionId.equals(other.regionId)
                && name.equals(other.name);
    }

    @Override
    public String toString() {
        return "Region{regionId=" + regionId + ", name=" + name + "}";
    }
}
