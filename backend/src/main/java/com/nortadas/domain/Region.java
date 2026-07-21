package com.nortadas.domain;

import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.RegionId;

/**
 * A coastal region grouping the beaches located within it (e.g. Norte, Centro,
 * Lisboa, Alentejo, Algarve). Knows its own identity and name; weather and
 * Nortada status are a {@link Beach}/{@link WeatherReading} concern.
 *
 * <p>Equality is identity-based ({@link RegionId}), as for any domain entity.
 */
public class Region {

    private final RegionId regionId;
    private final Name name;

    /**
     * Creates a new region, generating its own identity from its name (GRASP
     * Creator). The name-derived prefix inside the id is a snapshot at creation:
     * renaming a region later does not change its identity.
     */
    public Region(Name name) {
        this(RegionId.newId(name), name);
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

    @Override
    public String toString() {
        return "Region{regionId=" + regionId + ", name=" + name + "}";
    }
}
