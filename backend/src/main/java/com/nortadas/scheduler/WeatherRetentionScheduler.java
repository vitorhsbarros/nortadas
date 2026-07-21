package com.nortadas.scheduler;

import com.nortadas.application.usecase.PurgeOldWeatherReadingsUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Driving adapter that triggers the daily weather-retention purge (issue #48,
 * docs/architecture.md §2). Like {@link WeatherDataScheduler} it is intentionally
 * thin — it holds no business logic, only the schedule, and delegates to
 * {@link PurgeOldWeatherReadingsUseCase}.
 *
 * <p>The trigger interval is configurable via {@code nortadas.weather.retention.cron}
 * (top of every day by default — a purge only needs to run once a day, not
 * hourly). The bean itself is gated behind {@code nortadas.weather.retention.enabled}
 * (default {@code true}) so it can be switched off entirely — the {@code test}
 * profile disables it so the context loads without a scheduled purge ever firing.
 * Scheduling itself is enabled once, in
 * {@link com.nortadas.config.SchedulingConfig}.
 */
@Component
@ConditionalOnProperty(name = "nortadas.weather.retention.enabled", havingValue = "true", matchIfMissing = true)
public class WeatherRetentionScheduler {

    private final PurgeOldWeatherReadingsUseCase purgeOldWeatherReadingsUseCase;

    public WeatherRetentionScheduler(PurgeOldWeatherReadingsUseCase purgeOldWeatherReadingsUseCase) {
        this.purgeOldWeatherReadingsUseCase = purgeOldWeatherReadingsUseCase;
    }

    @Scheduled(cron = "${nortadas.weather.retention.cron}")
    public void purgeOldWeatherReadings() {
        purgeOldWeatherReadingsUseCase.purgeExpiredReadings();
    }
}
