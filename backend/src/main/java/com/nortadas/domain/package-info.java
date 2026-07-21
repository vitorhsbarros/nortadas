/**
 * Domain layer — entities, value objects and (later) domain services holding the
 * business rules.
 *
 * <p><strong>Pure Java only</strong> (docs/architecture.md §3.1): zero framework
 * dependencies of any kind — no Spring, no JPA/Hibernate, no Jackson, and no
 * Lombok. Constructors enforce invariants; getters, {@code equals}/{@code hashCode}
 * and {@code toString} are hand-written. Depends on nothing outside the JDK.
 */
package com.nortadas.domain;
