package com.nortadas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The enum has no behaviour yet (the grading rule arrives with US010), but its
 * members and their severity ordering are part of the domain contract other
 * code will rely on (e.g. comparisons and persistence by name).
 */
class NortadaStatusTest {

    @Test
    @DisplayName("declares exactly the five grades in ascending severity order")
    void declaresFiveGradesInAscendingSeverityOrder() {
        assertArrayEquals(
                new NortadaStatus[]{
                        NortadaStatus.NONE,
                        NortadaStatus.LIGHT,
                        NortadaStatus.MODERATE,
                        NortadaStatus.STRONG,
                        NortadaStatus.SEVERE
                },
                NortadaStatus.values());
    }

    @Test
    @DisplayName("valueOf round-trips each name (persistence-by-name safety)")
    void valueOfRoundTripsEachName() {
        for (NortadaStatus status : NortadaStatus.values()) {
            assertEquals(status, NortadaStatus.valueOf(status.name()));
        }
    }
}
