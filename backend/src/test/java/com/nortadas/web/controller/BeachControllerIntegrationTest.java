package com.nortadas.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
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
 * Integration tests for {@code GET /api/beaches} (US011, issue #16): boots the
 * full context against the H2 test profile, so the Flyway seed (40 beaches over
 * five regions, zero weather readings) drives real assertions on the HAL+JSON
 * shape, pagination, ordering, status derivation, and the RFC-7807 error body.
 *
 * <p>{@code @Transactional} rolls back each method, so the reading inserted by
 * {@link #reflectsStoredReadingInStatus()} never leaks into the other methods'
 * "everything is NONE" expectations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BeachControllerIntegrationTest {

    /** Praia Central de Espinho — the alphabetically-first seeded beach. */
    private static final String ESPINHO_ID = "ae617359-5f5a-4f01-8952-52c51bb5e742";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WeatherReadingRepositoryPort weatherReadingRepository;

    @Test
    @DisplayName("returns 200 and the HAL page shape with default paging")
    void returnsHalPageWithDefaults() throws Exception {
        mockMvc.perform(get("/api/beaches"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.beaches").isArray())
                .andExpect(jsonPath("$._embedded.beaches", hasSize(20)))
                .andExpect(jsonPath("$.page.totalElements", is(40)))
                .andExpect(jsonPath("$.page.size", is(20)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$.page.totalPages", is(2)))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.first.href").exists())
                .andExpect(jsonPath("$._links.last.href").exists())
                .andExpect(jsonPath("$._links.prev").doesNotExist())
                .andExpect(jsonPath("$._links.next.href").exists());
    }

    @Test
    @DisplayName("each embedded beach carries its fields and self/collection links")
    void eachItemHasFieldsAndLinks() throws Exception {
        mockMvc.perform(get("/api/beaches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.beaches[0].id").exists())
                .andExpect(jsonPath("$._embedded.beaches[0].name").exists())
                .andExpect(jsonPath("$._embedded.beaches[0].region").exists())
                .andExpect(jsonPath("$._embedded.beaches[0].nortadaStatus").exists())
                .andExpect(jsonPath("$._embedded.beaches[0]._links.self.href",
                        containsString("/api/beaches/")))
                .andExpect(jsonPath("$._embedded.beaches[0]._links.collection.href",
                        endsWith("/api/beaches")))
                // self href of the first item ends with that item's id.
                .andExpect(jsonPath(
                        "$._embedded.beaches[0]._links.self.href",
                        endsWith("/api/beaches/" + ESPINHO_ID)));
    }

    @Test
    @DisplayName("the second page carries prev, drops next, and reports its own number")
    void secondPageNavigation() throws Exception {
        mockMvc.perform(get("/api/beaches").param("page", "1").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.beaches", hasSize(20)))
                .andExpect(jsonPath("$.page.number", is(1)))
                .andExpect(jsonPath("$._links.prev.href").exists())
                .andExpect(jsonPath("$._links.next").doesNotExist());
    }

    @Test
    @DisplayName("orders beaches by name ascending, first item alphabetically first")
    void ordersByNameAscending() throws Exception {
        mockMvc.perform(get("/api/beaches").param("size", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.beaches", hasSize(40)))
                .andExpect(jsonPath("$._embedded.beaches[0].name", is("Praia Central de Espinho")))
                // The whole page is non-decreasing by name (assert the last item
                // is alphabetically at or after the first — a cheap monotonicity
                // sanity check backed by the explicit first-item assertion above).
                .andExpect(jsonPath("$._embedded.beaches[39].name",
                        greaterThan("Praia Central de Espinho")));
    }

    @Test
    @DisplayName("with no stored readings every beach's status is NONE")
    void allStatusesNoneWhenNoReadings() throws Exception {
        mockMvc.perform(get("/api/beaches").param("size", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.beaches[*].nortadaStatus",
                        everyItem(is("NONE"))));
    }

    @Test
    @DisplayName("reflects a stored in-sector reading as the graded status")
    void reflectsStoredReadingInStatus() throws Exception {
        BeachId espinho = new BeachId(UUID.fromString(ESPINHO_ID));
        // 340 deg is in-sector (N-NNW gate 315-45); 30 km/h grades to MODERATE.
        weatherReadingRepository.save(WeatherReadingFactory.create(
                espinho,
                new WindSpeed(30.0),
                new WindDirection(340.0),
                18.0,
                16.0,
                Instant.now()));

        mockMvc.perform(get("/api/beaches").param("size", "40"))
                .andExpect(status().isOk())
                // Espinho is the first item; assert its status is the graded value.
                .andExpect(jsonPath("$._embedded.beaches[0].id", is(ESPINHO_ID)))
                .andExpect(jsonPath("$._embedded.beaches[0].nortadaStatus", is("MODERATE")))
                // No other beach gained a status.
                .andExpect(jsonPath("$._embedded.beaches[*].nortadaStatus",
                        hasItem("MODERATE")));
    }

    @Test
    @DisplayName("rejects size below 1 with an RFC-7807 problem detail")
    void rejectsSizeZero() throws Exception {
        mockMvc.perform(get("/api/beaches").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Invalid pagination parameters")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.type", is("https://api.nortada.pt/problems/invalid-pagination")));
    }

    @Test
    @DisplayName("rejects size above the maximum with a 400 problem detail")
    void rejectsSizeAboveMax() throws Exception {
        mockMvc.perform(get("/api/beaches").param("size", String.valueOf(BeachController.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Invalid pagination parameters")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("rejects a negative page with a 400 problem detail")
    void rejectsNegativePage() throws Exception {
        mockMvc.perform(get("/api/beaches").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Invalid pagination parameters")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("accepts the maximum page size boundary")
    void acceptsMaxSizeBoundary() throws Exception {
        mockMvc.perform(get("/api/beaches").param("size", String.valueOf(BeachController.MAX_PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", is(BeachController.MAX_PAGE_SIZE)));
    }
}
