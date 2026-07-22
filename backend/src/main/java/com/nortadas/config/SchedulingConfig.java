package com.nortadas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduling support (docs/architecture.md §2) so
 * {@code @Scheduled} triggers such as
 * {@link com.nortadas.scheduler.WeatherDataScheduler} run. Kept out of
 * {@code NortadasApplication} to keep the entry point free of cross-cutting
 * concerns.
 *
 * <p>Enabling scheduling here is harmless on its own: whether any job actually
 * runs is governed by the individual {@code @Scheduled} bean, which the
 * {@code test} profile disables via {@code nortadas.weather.scheduler.enabled=false}.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
