package com.nortadas.domain.municipality;

import com.nortadas.domain.region.Region;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;

/**
 * A Portuguese municipality, grouping the beaches located within it and
 * belonging to exactly one coastal {@link Region}. Sits between
 * {@link com.nortadas.domain.beach.Beach} and {@code Region} so beaches can be
 * filtered at a finer granularity than the seven NUTS-II regions as the
 * catalogue grows.
 *
 * <p>Equality is identity-based ({@link MunicipalityId}), as for any domain
 * entity: two municipalities are {@code equals} when they share an id,
 * regardless of name or region. To compare descriptive state as well, use
 * {@link #sameAs(Municipality)}.
 *
 * <p>Unlike {@link Region}/{@link com.nortadas.domain.beach.Beach}, a
 * municipality's identity is always an externally-known real-world code (its
 * INE/DICOFRE code) rather than one this system derives or generates, so there
 * is only one way to build one — no separate create-vs-rehydrate split.
 * Construction is factorized in {@link MunicipalityFactory} (GoF Factory /
 * GRASP Creator): callers outside this package go through
 * {@code MunicipalityFactory.create} rather than this constructor directly.
 */
public class Municipality {

    private final MunicipalityId municipalityId;
    private final Name name;
    private final Region region;

    Municipality(MunicipalityId municipalityId, Name name, Region region) {

        if (municipalityId == null) {
            throw new IllegalArgumentException("Municipality id cannot be null!");
        }

        if (name == null) {
            throw new IllegalArgumentException("Municipality name cannot be null!");
        }

        if (region == null) {
            throw new IllegalArgumentException("Municipality region cannot be null!");
        }

        this.municipalityId = municipalityId;
        this.name = name;
        this.region = region;
    }

    public MunicipalityId getMunicipalityId() {
        return municipalityId;
    }

    public Name getName() {
        return name;
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Municipality)) {
            return false;
        }
        Municipality that = (Municipality) other;
        return municipalityId.equals(that.municipalityId);
    }

    @Override
    public int hashCode() {
        return municipalityId.hashCode();
    }

    /**
     * Attribute-based comparison: {@code true} only when {@code other} is a
     * municipality with the same identity <em>and</em> the same name and region
     * (i.e. identical state).
     *
     * <p>Contrast with {@link #equals(Object)}: two municipalities with the same
     * id but different names/regions are {@code equals} (the same entity) yet
     * not {@code sameAs} (different state); municipalities with different ids
     * are neither. Null-safe — returns {@code false} for a {@code null}
     * argument.
     */
    public boolean sameAs(Municipality other) {
        if (other == null) {
            return false;
        }
        return municipalityId.equals(other.municipalityId)
                && name.equals(other.name)
                && region.equals(other.region);
    }

    @Override
    public String toString() {
        return "Municipality{municipalityId=" + municipalityId + ", name=" + name
                + ", region=" + region + "}";
    }
}
