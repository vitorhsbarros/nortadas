package com.nortadas.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nortadas.application.port.BeachRepositoryPort;
import com.nortadas.application.port.WeatherClientPort;
import com.nortadas.application.port.WeatherReadingRepositoryPort;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.weatherreading.WeatherReading;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link FetchWeatherUseCase} — the US009 sweep. The critical
 * acceptance criterion is that a per-beach failure is logged and skipped without
 * aborting the run or propagating, so these tests focus on that isolation.
 */
@ExtendWith(MockitoExtension.class)
class FetchWeatherUseCaseTest {

    @Mock
    private BeachRepositoryPort beachRepository;

    @Mock
    private WeatherClientPort weatherClient;

    @Mock
    private WeatherReadingRepositoryPort weatherReadingRepository;

    private FetchWeatherUseCase useCase() {
        return new FetchWeatherUseCase(beachRepository, weatherClient, weatherReadingRepository);
    }

    /** A Beach stub that only answers the accessors the use case reads. */
    private Beach beachStub(String name) {
        Beach beach = org.mockito.Mockito.mock(Beach.class);
        org.mockito.Mockito.lenient().when(beach.getBeachId()).thenReturn(BeachId.newId());
        org.mockito.Mockito.lenient().when(beach.getLatitude()).thenReturn(new Latitude(41.0));
        org.mockito.Mockito.lenient().when(beach.getLongitude()).thenReturn(new Longitude(-8.6));
        org.mockito.Mockito.lenient().when(beach.getName()).thenReturn(new Name(name));
        return beach;
    }

    @Test
    @DisplayName("fetches and stores exactly one reading per beach on the happy path")
    void storesOneReadingPerBeach() {
        Beach first = beachStub("Matosinhos");
        Beach second = beachStub("Espinho");
        when(beachRepository.findAll()).thenReturn(List.of(first, second));
        when(weatherClient.fetchCurrent(any(), any(), any()))
                .thenReturn(org.mockito.Mockito.mock(WeatherReading.class));

        useCase().fetchAllBeaches();

        verify(weatherClient).fetchCurrent(first.getBeachId(), first.getLatitude(), first.getLongitude());
        verify(weatherClient).fetchCurrent(second.getBeachId(), second.getLatitude(), second.getLongitude());
        verify(weatherReadingRepository, times(2)).save(any(WeatherReading.class));
    }

    @Test
    @DisplayName("does nothing and touches neither client nor repository for an empty catalogue")
    void doesNothingForEmptyCatalogue() {
        when(beachRepository.findAll()).thenReturn(List.of());

        useCase().fetchAllBeaches();

        verifyNoInteractions(weatherClient);
        verifyNoInteractions(weatherReadingRepository);
    }

    @Test
    @DisplayName("isolates a client failure: still processes the remaining beaches and never propagates")
    void isolatesClientFailure() {
        Beach failing = beachStub("Broken");
        Beach healthy = beachStub("Healthy");
        when(beachRepository.findAll()).thenReturn(List.of(failing, healthy));

        WeatherReading healthyReading = org.mockito.Mockito.mock(WeatherReading.class);
        when(weatherClient.fetchCurrent(failing.getBeachId(), failing.getLatitude(), failing.getLongitude()))
                .thenThrow(new RuntimeException("upstream 503"));
        when(weatherClient.fetchCurrent(healthy.getBeachId(), healthy.getLatitude(), healthy.getLongitude()))
                .thenReturn(healthyReading);

        useCase().fetchAllBeaches();

        // The failing beach's reading is never saved; the healthy one still is.
        verify(weatherReadingRepository).save(healthyReading);
        verify(weatherReadingRepository, times(1)).save(any(WeatherReading.class));
    }

    @Test
    @DisplayName("isolates a repository failure: the sweep continues to the next beach and never propagates")
    void isolatesRepositoryFailure() {
        Beach failing = beachStub("Broken");
        Beach healthy = beachStub("Healthy");
        when(beachRepository.findAll()).thenReturn(List.of(failing, healthy));

        WeatherReading failingReading = org.mockito.Mockito.mock(WeatherReading.class);
        WeatherReading healthyReading = org.mockito.Mockito.mock(WeatherReading.class);
        when(weatherClient.fetchCurrent(failing.getBeachId(), failing.getLatitude(), failing.getLongitude()))
                .thenReturn(failingReading);
        when(weatherClient.fetchCurrent(healthy.getBeachId(), healthy.getLatitude(), healthy.getLongitude()))
                .thenReturn(healthyReading);
        doThrow(new RuntimeException("db down")).when(weatherReadingRepository).save(failingReading);

        useCase().fetchAllBeaches();

        // Save was attempted for the failing beach and still reached the healthy one.
        verify(weatherReadingRepository).save(failingReading);
        verify(weatherReadingRepository).save(healthyReading);
    }

    @Test
    @DisplayName("a failure on the last beach does not stop earlier beaches from being stored")
    void earlierBeachesStoredWhenLastFails() {
        Beach healthy = beachStub("Healthy");
        Beach failing = beachStub("Broken");
        when(beachRepository.findAll()).thenReturn(List.of(healthy, failing));

        WeatherReading healthyReading = org.mockito.Mockito.mock(WeatherReading.class);
        when(weatherClient.fetchCurrent(healthy.getBeachId(), healthy.getLatitude(), healthy.getLongitude()))
                .thenReturn(healthyReading);
        when(weatherClient.fetchCurrent(failing.getBeachId(), failing.getLatitude(), failing.getLongitude()))
                .thenThrow(new RuntimeException("upstream timeout"));

        useCase().fetchAllBeaches();

        verify(weatherReadingRepository).save(healthyReading);
        verify(weatherReadingRepository, times(1)).save(any(WeatherReading.class));
    }
}
