package com.nortadas.domain.region;

import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.RegionId;

/**
 * Factorizes {@link Region} construction (GoF Factory, GRASP Creator;
 * docs/architecture.md §6, §7): the sole public entry point for building
 * regions from outside this package, so callers never have to choose between
 * {@code Region}'s create/rehydrate constructors themselves.
 */
public final class RegionFactory {

    private RegionFactory() {
    }

    /** Creates a new region, deriving its own identity from its name. */
    public static Region create(Name name) {
        return new Region(name);
    }

    /** Rehydrates a region with a known identity (e.g. loaded from persistence). */
    public static Region rehydrate(RegionId regionId, Name name) {
        return new Region(regionId, name);
    }
}
