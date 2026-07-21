package com.nortadas.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.nortadas.application.usecase.PurgeOldWeatherReadingsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link WeatherRetentionScheduler}: it is a thin driving adapter,
 * so the only behaviour worth pinning down is that the scheduled trigger delegates
 * to the use case and adds no logic of its own.
 */
@ExtendWith(MockitoExtension.class)
class WeatherRetentionSchedulerTest {

    @Mock
    private PurgeOldWeatherReadingsUseCase purgeOldWeatherReadingsUseCase;

    @InjectMocks
    private WeatherRetentionScheduler scheduler;

    @Test
    @DisplayName("the scheduled trigger delegates to PurgeOldWeatherReadingsUseCase.purgeExpiredReadings")
    void delegatesToUseCase() {
        scheduler.purgeOldWeatherReadings();

        verify(purgeOldWeatherReadingsUseCase).purgeExpiredReadings();
        verifyNoMoreInteractions(purgeOldWeatherReadingsUseCase);
    }
}
