/**
 * The {@code Region} aggregate — {@code Region}, the aggregate root only. Its
 * identity value object, {@code RegionId}, lives in
 * {@code com.nortadas.domain.valueobject} alongside every other value object in
 * the domain. Groups the beaches located within it but does not own or
 * reference {@code Beach} instances (that association is held the other way
 * round, by {@code com.nortadas.domain.beach.Beach}). Pure Java only, like the
 * rest of the domain layer (docs/architecture.md §3.1).
 */
package com.nortadas.domain.region;
