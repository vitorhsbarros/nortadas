package com.nortadas.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The collection of beaches a single user has favourited. Aggregates references
 * to {@link Beach}; it does not own or duplicate beach data.
 *
 * <p>The owning {@code User} and any persistence of favourites are out of Phase 1
 * scope (see the scope note in {@code docs/OOA/class-responsibilities.md}); this
 * type exists so the domain model matches the analysis, and will gain its user
 * linkage when the corresponding user stories land.
 */
public class FavouriteBeaches {

    private final Set<Beach> beaches = new LinkedHashSet<>();

    /** Adds a beach to the favourites; adding an already-favourited beach is a no-op. */
    public void add(Beach beach) {

        if (beach == null) {
            throw new IllegalArgumentException("Cannot favourite a null beach!");
        }

        beaches.add(beach);
    }

    /** Removes a beach from the favourites; removing an absent beach is a no-op. */
    public void remove(Beach beach) {

        if (beach == null) {
            throw new IllegalArgumentException("Cannot unfavourite a null beach!");
        }

        beaches.remove(beach);
    }

    public boolean contains(Beach beach) {
        return beach != null && beaches.contains(beach);
    }

    /** An unmodifiable view of the favourited beaches, in insertion order. */
    public Set<Beach> getBeaches() {
        return Collections.unmodifiableSet(beaches);
    }

    @Override
    public String toString() {
        return "FavouriteBeaches{count=" + beaches.size() + "}";
    }
}
