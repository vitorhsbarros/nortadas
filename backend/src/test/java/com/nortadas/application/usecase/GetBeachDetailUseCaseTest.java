package com.nortadas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nortadas.application.port.BeachRepositoryPort;
import com.nortadas.application.port.WeatherReadingRepositoryPort;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.beach.BeachFactory;
import com.nortadas.domain.municipality.Municipality;
import com.nortadas.domain.municipality.MunicipalityFactory;
import com.nortadas.domain.region.Region;
import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.service.NortadaDetectionService;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.weatherreading.WeatherReading;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure-JUnit unit tests for {@link GetBeachDetailUseCase} (US012): the three
 * paths of {@code getBeach} — found without a reading, found with a reading, and
 * unknown id — with every collaborator mocked. No Spring context: the use case is
 * plain application logic (docs/architecture.md §9).
 */
@ExtendWith(MockitoExtension.class)
class GetBeachDetailUseCaseTest {

    @Mock
    private BeachRepositoryPort beachRepository;

    @Mock
    private WeatherReadingRepositoryPort weatherReadingRepository;

    @Mock
    private NortadaDetectionService detectionService;

    private final Region region = RegionFactory.create(new Name("Norte"));
    private final Municipality municipality =
            MunicipalityFactory.create(MunicipalityId.of("0107"), new Name("Espinho"), region);

    private GetBeachDetailUseCase useCase() {
        return new GetBeachDetailUseCase(beachRepository, weatherReadingRepository, detectionService);
    }

    /** Builds a beach with the given name and a fresh random identity. */
    private Beach beach(String name) {
        return BeachFactory.create(new Name(name), new Latitude(41.0), new Longitude(-8.6), municipality);
    }

    @Test
    @DisplayName("returns the beach with NONE and no reading, never calling the detection service")
    void gradesMissingReadingAsNoneWithoutDetecting() {
        Beach found = beach("Praia Central de Espinho");
        BeachId id = found.getBeachId();
        when(beachRepository.findById(id)).thenReturn(Optional.of(found));
        when(weatherReadingRepository.findLatestByBeachId(id)).thenReturn(Optional.empty());

        BeachStatusView view = useCase().getBeach(id);

        assertThat(view.beach()).isSameAs(found);
        assertThat(view.status()).isEqualTo(NortadaStatus.NONE);
        assertThat(view.latestReading()).isEmpty();
        verify(detectionService, never()).detect(any());
    }

    @Test
    @DisplayName("derives the status from the detection service when a reading is present")
    void derivesStatusFromDetectionWhenReadingPresent() {
        Beach found = beach("Praia Central de Espinho");
        BeachId id = found.getBeachId();
        WeatherReading reading = Mockito.mock(WeatherReading.class);
        when(beachRepository.findById(id)).thenReturn(Optional.of(found));
        when(weatherReadingRepository.findLatestByBeachId(id)).thenReturn(Optional.of(reading));
        when(detectionService.detect(reading)).thenReturn(NortadaStatus.STRONG);

        BeachStatusView view = useCase().getBeach(id);

        assertThat(view.beach()).isSameAs(found);
        assertThat(view.status()).isEqualTo(NortadaStatus.STRONG);
        assertThat(view.latestReading()).contains(reading);
        verify(detectionService).detect(reading);
    }

    @Test
    @DisplayName("throws BeachNotFoundException for an unknown id without touching reading or detection")
    void throwsWhenBeachNotFound() {
        BeachId id = new BeachId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        when(beachRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().getBeach(id))
                .isInstanceOf(BeachNotFoundException.class)
                .hasMessageContaining(id.getValue().toString())
                .extracting(ex -> ((BeachNotFoundException) ex).getBeachId())
                .isEqualTo(id);

        verifyNoInteractions(weatherReadingRepository);
        verifyNoInteractions(detectionService);
    }
}
