package com.nortadas.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.nortadas.application.port.BeachRepositoryPort;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.valueobject.BeachId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the US008 Flyway seed migration and the persistence slice together:
 * boots the full context against the H2 test profile (so V2 actually runs), then
 * loads every beach back through {@link BeachRepositoryPort}. Because the adapter
 * maps each row through the domain constructors, this also proves every seeded
 * value satisfies the domain invariants (Name rules, Latitude/Longitude bounds).
 */
@SpringBootTest
@ActiveProfiles("test")
class SeededBeachDataTest {

    @Autowired
    private BeachRepositoryPort beachRepository;

    @Test
    void seedsTwentyNorteBeachesLoadableAsDomainObjects() {
        List<Beach> beaches = beachRepository.findAll();

        assertThat(beaches).hasSize(20);
        assertThat(beaches)
                .allSatisfy(beach ->
                        assertThat(beach.getRegion().getName().getValue()).isEqualTo("Norte"));
        assertThat(beaches)
                .extracting(beach -> beach.getName().getValue())
                .contains("Praia de Moledo", "Praia Central de Espinho");
    }

    @Test
    void loadsASeededBeachByIdWithItsCoordinatesMunicipalityAndRegion() {
        BeachId espinho = new BeachId(UUID.fromString("ae617359-5f5a-4f01-8952-52c51bb5e742"));

        Beach beach = beachRepository.findById(espinho).orElseThrow();

        assertThat(beach.getName().getValue()).isEqualTo("Praia Central de Espinho");
        assertThat(beach.getLatitude().getDegrees()).isEqualTo(41.0083);
        assertThat(beach.getLongitude().getDegrees()).isEqualTo(-8.6428);
        assertThat(beach.getMunicipality().getName().getValue()).isEqualTo("Espinho");
        assertThat(beach.getMunicipality().getMunicipalityId().getValue()).isEqualTo("0107");
        assertThat(beach.getRegion().getName().getValue()).isEqualTo("Norte");
        assertThat(beach.getRegion().getRegionId().getValue()).isEqualTo("NOR");
    }

    @Test
    void returnsEmptyForAnUnknownBeachId() {
        BeachId unknown = new BeachId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        assertThat(beachRepository.findById(unknown)).isEmpty();
    }
}
