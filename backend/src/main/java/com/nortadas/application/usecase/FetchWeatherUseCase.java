package com.nortadas.application.usecase;

import com.nortadas.application.port.BeachRepositoryPort;
import com.nortadas.application.port.WeatherClientPort;
import com.nortadas.application.port.WeatherReadingRepositoryPort;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.weatherreading.WeatherReading;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case that fetches and stores the current weather for every beach (Facade;
 * docs/architecture.md §7, ADR-006). One coordinating method,
 * {@link #fetchAllBeaches()}, that the scheduler drives hourly (US009).
 *
 * <p>Each beach's fetch-and-save is isolated in its own try/catch: a failure for
 * one beach is logged and the sweep moves on to the next, so a single flaky
 * upstream response never aborts the run or crashes the application (US009
 * acceptance criterion). Nortada detection is deliberately not invoked here — it
 * arrives with US010.
 */
@Service
public class FetchWeatherUseCase {

    private static final Logger log = LoggerFactory.getLogger(FetchWeatherUseCase.class);

    private final BeachRepositoryPort beachRepository;
    private final WeatherClientPort weatherClient;
    private final WeatherReadingRepositoryPort weatherReadingRepository;

    public FetchWeatherUseCase(BeachRepositoryPort beachRepository,
                               WeatherClientPort weatherClient,
                               WeatherReadingRepositoryPort weatherReadingRepository) {
        this.beachRepository = beachRepository;
        this.weatherClient = weatherClient;
        this.weatherReadingRepository = weatherReadingRepository;
    }

    /**
     * Fetches the current weather for every beach in the catalogue and stores one
     * {@link WeatherReading} per beach. Per-beach failures are isolated: they are
     * logged and skipped so the rest of the sweep still completes.
     */
    public void fetchAllBeaches() {
        List<Beach> beaches = beachRepository.findAll();
        log.info("Starting weather fetch for {} beach(es)", beaches.size());

        int stored = 0;
        for (Beach beach : beaches) {
            try {
                WeatherReading reading = weatherClient.fetchCurrent(
                        beach.getBeachId(), beach.getLatitude(), beach.getLongitude());
                weatherReadingRepository.save(reading);
                stored++;
            } catch (RuntimeException ex) {
                log.error("Failed to fetch/store weather for beach {} ({}): {}",
                        beach.getName().getValue(), beach.getBeachId(), ex.getMessage(), ex);
            }
        }

        log.info("Weather fetch complete: {}/{} beach(es) stored", stored, beaches.size());
    }
}
