package com.nortadas.application.usecase;

import com.nortadas.application.port.WeatherReadingRepositoryPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Use case that enforces the rolling retention window on stored weather readings
 * (Facade; docs/architecture.md §7, issue #48). One coordinating method,
 * {@link #purgeExpiredReadings()}, which the retention scheduler drives daily.
 *
 * <p>The window is strict: everything older than {@code now - retentionDays} is
 * deleted, with no keep-the-latest-per-beach exception — a beach whose readings
 * are all expired is simply left with none. The retention length is configurable
 * ({@code nortadas.weather.retention.days}) rather than hardcoded, and "now" is
 * read from an injected {@link Clock} so the cutoff is deterministic under test.
 */
@Service
public class PurgeOldWeatherReadingsUseCase {

    private static final Logger log = LoggerFactory.getLogger(PurgeOldWeatherReadingsUseCase.class);

    private final WeatherReadingRepositoryPort weatherReadingRepository;
    private final Clock clock;
    private final long retentionDays;

    public PurgeOldWeatherReadingsUseCase(WeatherReadingRepositoryPort weatherReadingRepository,
                                          Clock clock,
                                          @Value("${nortadas.weather.retention.days}") long retentionDays) {
        this.weatherReadingRepository = weatherReadingRepository;
        this.clock = clock;
        this.retentionDays = retentionDays;
    }

    /**
     * Deletes every stored reading older than the retention window and logs how
     * many rows were removed.
     */
    public void purgeExpiredReadings() {
        Instant cutoff = Instant.now(clock).minus(Duration.ofDays(retentionDays));
        log.info("Purging weather readings older than {} ({}-day retention window)", cutoff, retentionDays);

        int deleted = weatherReadingRepository.deleteOlderThan(cutoff);

        log.info("Weather retention purge complete: {} reading(s) removed", deleted);
    }
}
