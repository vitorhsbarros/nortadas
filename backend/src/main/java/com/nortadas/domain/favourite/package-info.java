/**
 * The {@code FavouriteBeaches} aggregate — a single user's collection of
 * favourited beaches. Aggregates references to
 * {@code com.nortadas.domain.beach.Beach} without owning or duplicating beach
 * data. Kept as its own aggregate rather than nested under {@code beach} because
 * it belongs conceptually to the (not-yet-implemented) owning user, not to any
 * one beach; see the scope note in
 * {@code docs/OOA/class-responsibilities.md} for the pending {@code User}
 * linkage. Pure Java only, like the rest of the domain layer
 * (docs/architecture.md §3.1).
 */
package com.nortadas.domain.favourite;
