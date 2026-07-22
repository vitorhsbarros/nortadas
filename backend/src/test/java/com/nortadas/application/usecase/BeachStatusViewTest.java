package com.nortadas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.beach.BeachFactory;
import com.nortadas.domain.municipality.MunicipalityFactory;
import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.NortadaStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link BeachStatusView} record's invariant checks: beach,
 * latestReading, and status must all be non-null (missing readings are modelled
 * with {@code Optional.empty()}, not {@code null}).
 */
class BeachStatusViewTest {

    private Beach beach() {
        return BeachFactory.create(
                new Name("Praia Test"),
                new Latitude(41.0),
                new Longitude(-8.6),
                MunicipalityFactory.create(
                        MunicipalityId.of("0107"),
                        new Name("Espinho"),
                        RegionFactory.create(new Name("Norte"))));
    }

    @Test
    @DisplayName("holds the beach, optional reading and status when all are present")
    void holdsItsComponents() {
        Beach beach = beach();
        BeachStatusView view = new BeachStatusView(beach, Optional.empty(), NortadaStatus.NONE);

        assertThat(view.beach()).isSameAs(beach);
        assertThat(view.latestReading()).isEmpty();
        assertThat(view.status()).isEqualTo(NortadaStatus.NONE);
    }

    @Test
    @DisplayName("rejects a null beach")
    void rejectsNullBeach() {
        assertThatThrownBy(() -> new BeachStatusView(null, Optional.empty(), NortadaStatus.NONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("beach");
    }

    @Test
    @DisplayName("rejects a null latestReading (Optional.empty() must be used instead)")
    void rejectsNullLatestReading() {
        Beach beach = beach();
        assertThatThrownBy(() -> new BeachStatusView(beach, null, NortadaStatus.NONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latestReading");
    }

    @Test
    @DisplayName("rejects a null status")
    void rejectsNullStatus() {
        Beach beach = beach();
        assertThatThrownBy(() -> new BeachStatusView(beach, Optional.empty(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status");
    }
}
