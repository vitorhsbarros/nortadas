/**
 * The Nortada detection domain service — the pure business rule that grades a
 * {@code com.nortadas.domain.weatherreading.WeatherReading} into a
 * {@code com.nortadas.domain.valueobject.NortadaStatus} (US010).
 *
 * <p>Detection is modelled with the GoF <em>Strategy</em> pattern (ADR-005,
 * docs/architecture.md §7): {@code NortadaDetectionStrategy} is the pluggable
 * rule seam, {@code SectorSpeedDetectionStrategy} is the default rule (the
 * N–NNW wind-sector gate plus graded km/h speed thresholds), and
 * {@code NortadaDetectionService} is the entry point the application layer
 * calls. This is a domain service — not a value object and not an aggregate.
 * Pure Java only, like the rest of the domain layer (docs/architecture.md
 * §3.1): no Spring, no Lombok, no framework of any kind.
 */
package com.nortadas.domain.service;
