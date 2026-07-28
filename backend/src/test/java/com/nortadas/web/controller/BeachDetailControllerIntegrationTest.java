package com.nortadas.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nortadas.application.port.WeatherReadingRepositoryPort;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import com.nortadas.domain.weatherreading.WeatherReadingFactory;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@code GET /api/beaches/{id}} (US012, issue #17): boots
 * the full context against the H2 test profile, so the Flyway seed (40 beaches,
 * zero weather readings) drives real assertions on the HAL+JSON detail shape, the
 * optional {@code reading} block, status derivation, and the RFC-7807 404 body.
 *
 * <p>Kept separate from {@code BeachControllerIntegrationTest} (the list endpoint)
 * so this story's test additions are self-contained. {@code @Transactional} rolls
 * back each method, so the reading inserted by
 * {@link #returnsGradedStatusAndReadingBlockWhenReadingPresent()} never leaks.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BeachDetailControllerIntegrationTest {

    /** Praia Central de Espinho — a known seeded beach. */
    private static final String ESPINHO_ID = "ae617359-5f5a-4f01-8952-52c51bb5e742";

    /** A well-formed but unseeded id. */
    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WeatherReadingRepositoryPort weatherReadingRepository;

    @Test
    @DisplayName("returns 200 with the beach fields, NONE status, and no reading block when unstored")
    void returnsBeachWithoutReadingBlockWhenNoReading() throws Exception {
        mockMvc.perform(get("/api/beaches/{id}", ESPINHO_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(ESPINHO_ID)))
                .andExpect(jsonPath("$.name", is("Praia Central de Espinho")))
                .andExpect(jsonPath("$.region").exists())
                .andExpect(jsonPath("$.nortadaStatus", is("NONE")))
                .andExpect(jsonPath("$.reading").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/beaches/" + ESPINHO_ID)))
                .andExpect(jsonPath("$._links.collection.href", endsWith("/api/beaches")));
    }

    @Test
    @DisplayName("returns the graded status and the reading block when a reading is stored")
    void returnsGradedStatusAndReadingBlockWhenReadingPresent() throws Exception {
        BeachId espinho = new BeachId(UUID.fromString(ESPINHO_ID));
        // 340 deg is in-sector (N-NNW gate); 30 km/h grades to MODERATE.
        weatherReadingRepository.save(WeatherReadingFactory.create(
                espinho,
                new WindSpeed(30.0),
                new WindDirection(340.0),
                18.0,
                16.0,
                Instant.parse("2026-07-20T09:00:00Z")));

        mockMvc.perform(get("/api/beaches/{id}", ESPINHO_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(ESPINHO_ID)))
                .andExpect(jsonPath("$.nortadaStatus", is("MODERATE")))
                .andExpect(jsonPath("$.reading.windSpeed", is(30.0)))
                .andExpect(jsonPath("$.reading.windDirection", is(340.0)))
                .andExpect(jsonPath("$.reading.temperature", is(18.0)))
                .andExpect(jsonPath("$.reading.fetchedAt", is("2026-07-20T09:00:00Z")));
    }

    @Test
    @DisplayName("returns an RFC-7807 404 problem detail for a well-formed but unknown id")
    void returns404ProblemDetailForUnknownId() throws Exception {
        mockMvc.perform(get("/api/beaches/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("Beach not found")))
                .andExpect(jsonPath("$.type", is("https://api.nortada.pt/problems/beach-not-found")))
                .andExpect(jsonPath("$.detail", containsString(UNKNOWN_ID)));
    }
}
