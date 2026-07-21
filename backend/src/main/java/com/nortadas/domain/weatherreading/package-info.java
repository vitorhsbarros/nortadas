/**
 * The {@code WeatherReading} aggregate — {@code WeatherReading}, the aggregate
 * root only, plus its {@code WeatherReadingFactory}. A weather reading is an
 * unbounded, per-beach time series, so it is its own aggregate: it references
 * {@code com.nortadas.domain.beach.Beach} by {@code BeachId} across the
 * aggregate boundary rather than being embedded in it, and carries its own
 * {@code WeatherReadingId} identity. Its value objects
 * ({@code WeatherReadingId}, {@code WindSpeed}, {@code WindDirection},
 * {@code BeachId}, ...) live in {@code com.nortadas.domain.valueobject}
 * alongside every other value object in the domain. Pure Java only, like the
 * rest of the domain layer (docs/architecture.md §3.1).
 */
package com.nortadas.domain.weatherreading;
