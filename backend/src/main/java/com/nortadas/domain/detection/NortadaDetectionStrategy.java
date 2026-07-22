package com.nortadas.domain.detection;

import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.weatherreading.WeatherReading;

/**
 * The pluggable Nortada detection rule (GoF Strategy; ADR-005,
 * docs/architecture.md §7). Implementations decide the {@link NortadaStatus} a
 * {@link WeatherReading} grades into, so an alternate rule set can be swapped in
 * without editing callers (OCP).
 */
public interface NortadaDetectionStrategy {

    /**
     * Grades a reading into a {@link NortadaStatus}.
     *
     * @param reading the observation to classify; never {@code null}
     * @return the detected status, never {@code null}
     */
    NortadaStatus detect(WeatherReading reading);
}
