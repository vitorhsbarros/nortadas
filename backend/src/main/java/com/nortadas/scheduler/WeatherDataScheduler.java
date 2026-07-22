package com.nortadas.scheduler;

import com.nortadas.application.usecase.FetchWeatherUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Driving adapter that triggers the hourly weather fetch (US009,
 * docs/architecture.md §2). It is intentionally thin — it holds no business
 * logic, only the schedule, and delegates to {@link FetchWeatherUseCase}.
 *
 * <p>The trigger interval is configurable via {@code nortadas.weather.scheduler.cron}
 * (top of every hour by default). The bean itself is gated behind
 * {@code nortadas.weather.scheduler.enabled} (default {@code true}) so it can be
 * switched off entirely — the {@code test} profile disables it so the context
 * loads without ever reaching out to the network.
 */
@Component
@ConditionalOnProperty(name = "nortadas.weather.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class WeatherDataScheduler {

    private final FetchWeatherUseCase fetchWeatherUseCase;

    public WeatherDataScheduler(FetchWeatherUseCase fetchWeatherUseCase) {
        this.fetchWeatherUseCase = fetchWeatherUseCase;
    }

    @Scheduled(cron = "${nortadas.weather.scheduler.cron}")
    public void fetchWeatherForAllBeaches() {
        fetchWeatherUseCase.fetchAllBeaches();
    }
}
