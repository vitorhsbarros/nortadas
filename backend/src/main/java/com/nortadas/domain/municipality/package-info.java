/**
 * The {@code Municipality} aggregate — {@code Municipality}, the aggregate root
 * only. Its identity value object, {@code MunicipalityId}, lives in
 * {@code com.nortadas.domain.valueobject} alongside every other value object in
 * the domain. Sits between {@code com.nortadas.domain.beach.Beach} and
 * {@code com.nortadas.domain.region.Region}: every municipality belongs to
 * exactly one region, and every beach belongs to exactly one municipality.
 * Pure Java only, like the rest of the domain layer (docs/architecture.md
 * §3.1).
 */
package com.nortadas.domain.municipality;
