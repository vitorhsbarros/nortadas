/**
 * Every value object in the domain — types with no identity and purely
 * value-based {@code equals}/{@code hashCode}, whether shared across aggregates
 * or local to a single one: {@code BeachId}, {@code MunicipalityId},
 * {@code RegionId}, {@code WeatherReadingId}, {@code Latitude},
 * {@code Longitude}, {@code WindSpeed}, {@code WindDirection}, {@code Name} and
 * {@code NortadaStatus}. {@code NortadaStatus} belongs here too — despite
 * reading like a small aggregate, it is immutable and compared by value, not by
 * identity, so it follows the same rule as the primitive-wrapping value objects
 * rather than sitting inside an aggregate package.
 *
 * <p>Aggregate root entities ({@code Beach}, {@code Municipality},
 * {@code Region}, {@code WeatherReading}, {@code FavouriteBeaches}) stay in
 * their own aggregate packages and depend on this package, never the other way
 * round. Pure Java only, like the rest of the domain layer
 * (docs/architecture.md §3.1).
 */
package com.nortadas.domain.valueobject;
