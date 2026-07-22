package com.nortadas.config;

import com.nortadas.domain.service.NortadaDetectionService;
import com.nortadas.domain.service.SectorSpeedDetectionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the Nortada detection domain service into the Spring context
 * (docs/architecture.md §1, §7, ADR-005). The domain classes are pure Java with
 * no Spring annotations (§3.1), so the choice of concrete rule is made here, in
 * the {@code config} layer, rather than in the domain: the default strategy is
 * {@link SectorSpeedDetectionStrategy} (the US010 N–NNW sector + graded-speed
 * rule). Swapping the rule is a one-line change here, with no edit to any
 * caller (OCP).
 */
@Configuration
public class DetectionConfig {

    @Bean
    public NortadaDetectionService nortadaDetectionService() {
        return new NortadaDetectionService(new SectorSpeedDetectionStrategy());
    }
}
