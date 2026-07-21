package com.nortadas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the application context starts — including datasource, Flyway
 * migration and security wiring — against the in-memory test profile (H2 in
 * PostgreSQL mode), so the build needs no running PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("test")
class NortadasApplicationTests {

    @Test
    void contextLoads() {
    }
}
