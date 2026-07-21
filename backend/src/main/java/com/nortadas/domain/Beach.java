package com.nortadas.domain;

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
 * <p>Equality is identity-based ({@link BeachId}), as for any domain entity.
 */
public class Beach {

    private final BeachId beachId;
    private final Name name;
    private final Latitude latitude;
    private final Longitude longitude;
    private final Region region;

    /** Creates a new beach, generating its own identity (GRASP Creator). */
    public Beach(Name name, Latitude latitude, Longitude longitude, Region region) {
        this(BeachId.newId(), name, latitude, longitude, region);
    }

    /** Rehydrates a beach with a known identity (e.g. loaded from persistence). */
    public Beach(BeachId beachId, Name name, Latitude latitude, Longitude longitude, Region region) {

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

    @Override
    public String toString() {
        return "Beach{beachId=" + beachId + ", name=" + name
                + ", latitude=" + latitude + ", longitude=" + longitude
                + ", region=" + region + "}";
    }
}
