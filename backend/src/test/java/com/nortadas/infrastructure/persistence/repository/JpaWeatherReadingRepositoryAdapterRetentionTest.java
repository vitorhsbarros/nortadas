package com.nortadas.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.nortadas.infrastructure.persistence.datamodel.WeatherReadingDataModel;
import com.nortadas.infrastructure.persistence.mapper.WeatherReadingMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Persistence-slice test for the issue #48 retention delete: proves the adapter's
 * {@code deleteOlderThan} (via the repository's bulk {@code @Modifying} delete)
 * removes readings strictly older than the cutoff and keeps readings at or after
 * it. Runs against the H2 test-profile datasource with Flyway (so the seeded
 * beaches exist to satisfy the {@code weather_reading -> beach} foreign key).
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaWeatherReadingRepositoryAdapter.class, WeatherReadingMapper.class})
class JpaWeatherReadingRepositoryAdapterRetentionTest {

    /** A seeded Norte beach (Praia Central de Espinho) satisfying the FK. */
    private static final UUID SEEDED_BEACH_ID =
            UUID.fromString("ae617359-5f5a-4f01-8952-52c51bb5e742");

    private static final Instant CUTOFF = Instant.parse("2026-07-14T00:00:00Z");

    @Autowired
    private JpaWeatherReadingRepositoryAdapter adapter;

    @Autowired
    private WeatherReadingJpaRepository jpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private WeatherReadingDataModel readingAt(Instant fetchedAt) {
        return new WeatherReadingDataModel(
                UUID.randomUUID(), SEEDED_BEACH_ID, 12.0, 0.0, 18.0, 16.0, fetchedAt);
    }

    @Test
    @DisplayName("deletes readings strictly older than the cutoff and keeps the rest")
    void deletesOnlyExpiredReadings() {
        WeatherReadingDataModel expiredA = readingAt(CUTOFF.minus(2, ChronoUnit.DAYS));
        WeatherReadingDataModel expiredB = readingAt(CUTOFF.minus(1, ChronoUnit.SECONDS));
        WeatherReadingDataModel atCutoff = readingAt(CUTOFF);
        WeatherReadingDataModel recent = readingAt(CUTOFF.plus(3, ChronoUnit.DAYS));
        jpaRepository.saveAll(List.of(expiredA, expiredB, atCutoff, recent));
        entityManager.flush();
        entityManager.clear();

        int deleted = adapter.deleteOlderThan(CUTOFF);

        assertThat(deleted).isEqualTo(2);
        assertThat(jpaRepository.findAll())
                .extracting(WeatherReadingDataModel::getId)
                .containsExactlyInAnyOrder(atCutoff.getId(), recent.getId());
    }

    @Test
    @DisplayName("deletes nothing and reports zero when every reading is newer than the cutoff")
    void deletesNothingWhenAllRecent() {
        jpaRepository.saveAll(List.of(
                readingAt(CUTOFF),
                readingAt(CUTOFF.plus(1, ChronoUnit.DAYS))));
        entityManager.flush();
        entityManager.clear();

        int deleted = adapter.deleteOlderThan(CUTOFF);

        assertThat(deleted).isZero();
        assertThat(jpaRepository.findAll()).hasSize(2);
    }
}
