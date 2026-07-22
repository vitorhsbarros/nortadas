package com.nortadas.domain.detection;

import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.weatherreading.WeatherReading;

/**
 * Domain entry point for Nortada detection (ADR-005, docs/architecture.md §5–§7).
 * It delegates to a {@link NortadaDetectionStrategy}, keeping the rule pluggable
 * (GoF Strategy, OCP) while giving the application layer a single, stable
 * collaborator to call.
 *
 * <p>The rule is injected: pass {@link SectorSpeedDetectionStrategy} for the
 * US010 rule (N–NNW sector gate + graded km/h thresholds), or an alternate
 * strategy to swap the rule without touching callers. The default is chosen
 * where the service is wired (the {@code config} layer), not hardcoded here.
 */
public final class NortadaDetectionService {

    private final NortadaDetectionStrategy strategy;

    /**
     * Uses the given detection rule.
     *
     * @param strategy the rule to delegate to; never {@code null}
     */
    public NortadaDetectionService(NortadaDetectionStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Detection strategy cannot be null!");
        }
        this.strategy = strategy;
    }

    /**
     * Detects the {@link NortadaStatus} for a reading by delegating to the
     * configured strategy.
     *
     * @param reading the observation to classify; never {@code null}
     * @return the detected status, never {@code null}
     */
    public NortadaStatus detect(WeatherReading reading) {
        return strategy.detect(reading);
    }
}
