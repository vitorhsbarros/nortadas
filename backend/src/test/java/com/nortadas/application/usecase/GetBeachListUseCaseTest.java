package com.nortadas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure-JUnit unit tests for {@link GetBeachListUseCase} (US011): sorting,
 * in-memory pagination slicing, and per-beach status derivation, with every
 * collaborator mocked. No Spring context — the use case is plain application
 * logic (docs/architecture.md §9).
 */
@ExtendWith(MockitoExtension.class)
class GetBeachListUseCaseTest {

    @Mock
    private BeachRepositoryPort beachRepository;

    @Mock
    private WeatherReadingRepositoryPort weatherReadingRepository;

    @Mock
    private NortadaDetectionService detectionService;

    private final Region region = RegionFactory.create(new Name("Norte"));
    private final Municipality municipality =
            MunicipalityFactory.create(MunicipalityId.of("0107"), new Name("Espinho"), region);

    private GetBeachListUseCase useCase() {
        return new GetBeachListUseCase(beachRepository, weatherReadingRepository, detectionService);
    }

    /** Builds a beach with the given name and a fresh random identity. */
    private Beach beach(String name) {
        return BeachFactory.create(new Name(name), new Latitude(41.0), new Longitude(-8.6), municipality);
    }

    private List<String> names(PageResult<BeachStatusView> page) {
        return page.content().stream().map(v -> v.beach().getName().getValue()).toList();
    }

    // ---- validation ---------------------------------------------------------

    @Test
    @DisplayName("rejects a negative page index")
    void rejectsNegativePage() {
        assertThatThrownBy(() -> useCase().getBeaches(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }

    @Test
    @DisplayName("rejects a non-positive page size")
    void rejectsNonPositiveSize() {
        assertThatThrownBy(() -> useCase().getBeaches(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }

    // ---- sorting ------------------------------------------------------------

    @Test
    @DisplayName("orders the page content by beach name ascending regardless of repository order")
    void ordersByNameAscending() {
        when(beachRepository.findAll())
                .thenReturn(List.of(beach("Charlie"), beach("Alpha"), beach("Bravo")));
        when(weatherReadingRepository.findLatestByBeachId(any())).thenReturn(Optional.empty());

        PageResult<BeachStatusView> page = useCase().getBeaches(0, 10);

        assertThat(names(page)).containsExactly("Alpha", "Bravo", "Charlie");
    }

    // ---- pagination slicing -------------------------------------------------

    @Test
    @DisplayName("returns disjoint, correctly-sliced pages and reports the full total on each")
    void slicesPagesDisjointly() {
        when(beachRepository.findAll()).thenReturn(List.of(
                beach("Alpha"), beach("Bravo"), beach("Charlie"), beach("Delta"), beach("Echo")));
        when(weatherReadingRepository.findLatestByBeachId(any())).thenReturn(Optional.empty());

        PageResult<BeachStatusView> first = useCase().getBeaches(0, 2);
        PageResult<BeachStatusView> second = useCase().getBeaches(1, 2);

        assertThat(names(first)).containsExactly("Alpha", "Bravo");
        assertThat(names(second)).containsExactly("Charlie", "Delta");
        assertThat(first.totalElements()).isEqualTo(5);
        assertThat(second.totalElements()).isEqualTo(5);
        assertThat(first.pageNumber()).isZero();
        assertThat(second.pageNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("returns the partial final page with only the remaining elements")
    void returnsPartialLastPage() {
        when(beachRepository.findAll()).thenReturn(List.of(
                beach("Alpha"), beach("Bravo"), beach("Charlie"), beach("Delta"), beach("Echo")));
        when(weatherReadingRepository.findLatestByBeachId(any())).thenReturn(Optional.empty());

        PageResult<BeachStatusView> last = useCase().getBeaches(2, 2);

        assertThat(names(last)).containsExactly("Echo");
        assertThat(last.totalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("returns empty content but the correct total for a page beyond the catalogue")
    void returnsEmptyContentBeyondEnd() {
        when(beachRepository.findAll())
                .thenReturn(List.of(beach("Alpha"), beach("Bravo"), beach("Charlie")));

        PageResult<BeachStatusView> beyond = useCase().getBeaches(5, 2);

        assertThat(beyond.content()).isEmpty();
        assertThat(beyond.totalElements()).isEqualTo(3);
        verify(weatherReadingRepository, never()).findLatestByBeachId(any());
    }

    @Test
    @DisplayName("returns empty content and total zero for an empty catalogue")
    void handlesEmptyCatalogue() {
        when(beachRepository.findAll()).thenReturn(List.of());

        PageResult<BeachStatusView> page = useCase().getBeaches(0, 20);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }

    // ---- status derivation --------------------------------------------------

    @Test
    @DisplayName("grades a beach with no reading as NONE without calling the detection service")
    void gradesMissingReadingAsNoneWithoutDetecting() {
        when(beachRepository.findAll()).thenReturn(List.of(beach("Alpha")));
        when(weatherReadingRepository.findLatestByBeachId(any())).thenReturn(Optional.empty());

        PageResult<BeachStatusView> page = useCase().getBeaches(0, 10);

        BeachStatusView view = page.content().get(0);
        assertThat(view.status()).isEqualTo(NortadaStatus.NONE);
        assertThat(view.latestReading()).isEmpty();
        verify(detectionService, never()).detect(any());
    }

    @Test
    @DisplayName("derives the status from the detection service when a reading is present")
    void derivesStatusFromDetectionWhenReadingPresent() {
        Beach alpha = beach("Alpha");
        WeatherReading reading = org.mockito.Mockito.mock(WeatherReading.class);
        when(beachRepository.findAll()).thenReturn(List.of(alpha));
        when(weatherReadingRepository.findLatestByBeachId(alpha.getBeachId()))
                .thenReturn(Optional.of(reading));
        when(detectionService.detect(reading)).thenReturn(NortadaStatus.STRONG);

        PageResult<BeachStatusView> page = useCase().getBeaches(0, 10);

        BeachStatusView view = page.content().get(0);
        assertThat(view.status()).isEqualTo(NortadaStatus.STRONG);
        assertThat(view.latestReading()).contains(reading);
        verify(detectionService).detect(reading);
    }

    @Test
    @DisplayName("grades only the beaches on the requested page, not the whole catalogue")
    void gradesOnlyThePageBeaches() {
        List<Beach> catalogue = new ArrayList<>();
        for (char c = 'A'; c <= 'E'; c++) {
            catalogue.add(beach("Beach" + c));
        }
        when(beachRepository.findAll()).thenReturn(catalogue);
        when(weatherReadingRepository.findLatestByBeachId(any())).thenReturn(Optional.empty());

        useCase().getBeaches(0, 2);

        // Only the 2 beaches on page 0 are looked up; the other 3 are untouched,
        // and NONE is never sent to the detection service.
        verify(weatherReadingRepository, times(2)).findLatestByBeachId(any());
        verify(detectionService, never()).detect(any());
    }
}
