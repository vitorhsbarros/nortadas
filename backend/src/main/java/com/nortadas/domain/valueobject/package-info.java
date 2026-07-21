/**
 * Every value object in the domain — types with no identity and purely
 * value-based {@code equals}/{@code hashCode}, whether shared across aggregates
 * or local to a single one: {@code BeachId}, {@code MunicipalityId},
 * {@code RegionId}, {@code Latitude}, {@code Longitude}, {@code WindSpeed},
 * {@code WindDirection}, {@code Name}, {@code WeatherReading} and
 * {@code NortadaStatus}. {@code WeatherReading} and {@code NortadaStatus}
 * belong here too — despite reading like small aggregates, both are immutable
 * and compared by value, not by identity, so they follow the same rule as the
 * primitive-wrapping value objects rather than sitting inside {@code beach}.
 *
 * <p>Aggregate root entities ({@code Beach}, {@code Municipality},
 * {@code Region}, {@code FavouriteBeaches}) stay in their own aggregate
 * packages and depend on this package, never the other way round. Pure Java
 * only, like the rest of the domain layer (docs/architecture.md §3.1).
 */
package com.nortadas.domain.valueobject;
