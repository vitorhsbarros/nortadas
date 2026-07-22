package com.nortadas.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the application {@link Clock} (docs/architecture.md §1, DIP). Time is
 * injected as a collaborator rather than read from {@code Instant.now()} at the
 * call site so time-dependent use cases — such as the retention cutoff in
 * {@link com.nortadas.application.usecase.PurgeOldWeatherReadingsUseCase} (issue
 * #48) — stay deterministic and unit-testable with a fixed clock.
 */
@Configuration
public class ClockConfig {

    /** UTC wall-clock time; overridable in tests with a fixed {@link Clock}. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
