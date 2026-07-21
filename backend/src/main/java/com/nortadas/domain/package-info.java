/**
 * Domain layer — entities, value objects and (later) domain services holding the
 * business rules. Aggregate roots each get their own sub-package holding only
 * the root entity itself: {@code beach} ({@code Beach}), {@code region}
 * ({@code Region}) and {@code favourite} ({@code FavouriteBeaches}). Every
 * value object in the domain — whether used by a single aggregate or shared
 * across several, e.g. {@code Name} — lives together in {@code valueobject}.
 * See each sub-package's {@code package-info.java} for its specific boundary.
 *
 * <p><strong>Pure Java only</strong> (docs/architecture.md §3.1): zero framework
 * dependencies of any kind — no Spring, no JPA/Hibernate, no Jackson, and no
 * Lombok. Constructors enforce invariants; getters, {@code equals}/{@code hashCode}
 * and {@code toString} are hand-written. Depends on nothing outside the JDK.
 */
package com.nortadas.domain;
