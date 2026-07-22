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
 * — {@code hh:23:37} by default, <strong>not</strong> the top of the hour
 * (issue #50). Live testing showed the scheduler firing at 13:00:00.000 sharp
 * got HTTP 503 "The service is overloaded" from Open-Meteo for every single
 * beach: hh:00:00 is the world's most common cron trigger time, so free-tier
 * Open-Meteo sees a synchronized global load spike right on it. Landing on a
 * non-round minute and a non-zero second avoids colliding with that
 * thundering herd. The bean itself is gated behind
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
