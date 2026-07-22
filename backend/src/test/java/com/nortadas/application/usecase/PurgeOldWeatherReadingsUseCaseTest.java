package com.nortadas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.nortadas.application.port.WeatherReadingRepositoryPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PurgeOldWeatherReadingsUseCase} — the issue #48 retention
 * purge. The behaviour worth pinning down is that it computes the cutoff as
 * {@code now - retentionDays} from the injected clock and delegates the actual
 * delete to the port, so a fixed {@link Clock} lets the exact cutoff be asserted.
 */
@ExtendWith(MockitoExtension.class)
class PurgeOldWeatherReadingsUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-07-21T10:15:30Z");

    @Mock
    private WeatherReadingRepositoryPort weatherReadingRepository;

    private PurgeOldWeatherReadingsUseCase useCase(long retentionDays) {
        Clock fixed = Clock.fixed(NOW, ZoneOffset.UTC);
        return new PurgeOldWeatherReadingsUseCase(weatherReadingRepository, fixed, retentionDays);
    }

    @Test
    @DisplayName("computes cutoff = now - retentionDays and delegates the delete to the port")
    void purgesWithCutoffFromClock() {
        when(weatherReadingRepository.deleteOlderThan(NOW.minus(Duration.ofDays(7)))).thenReturn(3);

        useCase(7).purgeExpiredReadings();

        ArgumentCaptor<Instant> cutoff = ArgumentCaptor.forClass(Instant.class);
        verify(weatherReadingRepository).deleteOlderThan(cutoff.capture());
        assertThat(cutoff.getValue()).isEqualTo(NOW.minus(Duration.ofDays(7)));
        verifyNoMoreInteractions(weatherReadingRepository);
    }

    @Test
    @DisplayName("honours a configured non-default retention window")
    void honoursConfiguredWindow() {
        useCase(30).purgeExpiredReadings();

        verify(weatherReadingRepository).deleteOlderThan(NOW.minus(Duration.ofDays(30)));
    }
}
