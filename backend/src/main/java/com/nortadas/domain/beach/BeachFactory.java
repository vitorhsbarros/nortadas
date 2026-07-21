package com.nortadas.domain.beach;

import com.nortadas.domain.municipality.Municipality;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.Name;

/**
 * Factorizes {@link Beach} construction (GoF Factory, GRASP Creator;
 * docs/architecture.md §6, §7): the sole public entry point for building
 * beaches from outside this package, so callers never have to choose between
 * {@code Beach}'s create/rehydrate constructors themselves.
 */
public final class BeachFactory {

    private BeachFactory() {
    }

    /** Creates a new beach, generating its own identity. */
    public static Beach create(Name name, Latitude latitude, Longitude longitude, Municipality municipality) {
        return new Beach(name, latitude, longitude, municipality);
    }

    /** Rehydrates a beach with a known identity (e.g. loaded from persistence). */
    public static Beach rehydrate(
            BeachId beachId, Name name, Latitude latitude, Longitude longitude, Municipality municipality) {
        return new Beach(beachId, name, latitude, longitude, municipality);
    }
}
