/**
 * The {@code Beach} aggregate — {@code Beach}, the aggregate root only. Its
 * value objects and value-object-like history/result types
 * ({@code BeachId}, {@code Latitude}, {@code Longitude}, {@code WeatherReading},
 * {@code NortadaStatus}, ...) live in {@code com.nortadas.domain.valueobject}
 * alongside every other value object in the domain. References
 * {@code com.nortadas.domain.municipality.Municipality} (the beach's containing
 * municipality, and, through it, its region) across the aggregate boundary.
 * Pure Java only, like the rest of the domain layer (docs/architecture.md
 * §3.1).
 */
package com.nortadas.domain.beach;
