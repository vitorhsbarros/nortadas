package com.nortadas.domain.municipality;

import com.nortadas.domain.region.Region;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;

/**
 * Factorizes {@link Municipality} construction (GoF Factory, GRASP Creator;
 * docs/architecture.md §6, §7): the sole public entry point for building
 * municipalities from outside this package.
 *
 * <p>Unlike {@code RegionFactory}/{@code BeachFactory}, there is only one way
 * to build a {@code Municipality} — its identity is always an externally-known
 * code, never generated or derived — so there is no separate {@code rehydrate}
 * method.
 */
public final class MunicipalityFactory {

    private MunicipalityFactory() {
    }

    /** Builds a municipality from its known identity, name and region. */
    public static Municipality create(MunicipalityId municipalityId, Name name, Region region) {
        return new Municipality(municipalityId, name, region);
    }
}
