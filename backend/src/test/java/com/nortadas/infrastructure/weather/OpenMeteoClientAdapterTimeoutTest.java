package com.nortadas.infrastructure.weather;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.nortadas.config.OpenMeteoHttpClientConfig;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/**
 * Proves the read timeout wired by {@link OpenMeteoHttpClientConfig} actually
 * fires (issue #50) — not merely that it is configured. A real
 * {@link ServerSocket} accepts the connection but never sends a response, so
 * only a read timeout can end the call; the test asserts {@code fetchCurrent}
 * throws promptly instead of hanging.
 */
class OpenMeteoClientAdapterTimeoutTest {

    private static final BeachId BEACH_ID = BeachId.newId();
    private static final Latitude LATITUDE = new Latitude(41.0);
    private static final Longitude LONGITUDE = new Longitude(-8.0);

    @Test
    @DisplayName("a slow (never-responding) endpoint trips the read timeout instead of hanging")
    void readTimeoutStopsASlowResponseFromHanging() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            // Accept the connection but never write a response, so the client
            // blocks on read until the configured read timeout fires.
            Thread accepter = new Thread(() -> {
                try (Socket socket = serverSocket.accept()) {
                    Thread.sleep(10_000);
                } catch (Exception ignored) {
                    // Test finishes and closes the socket first; nothing to do.
                }
            });
            accepter.setDaemon(true);
            accepter.start();

            RestClient.Builder builder = RestClient.builder();
            new OpenMeteoHttpClientConfig()
                    .openMeteoTimeoutCustomizer(Duration.ofMillis(200), Duration.ofMillis(200))
                    .customize(builder);

            String baseUrl = "http://localhost:" + serverSocket.getLocalPort();
            OpenMeteoClientAdapter adapter = new OpenMeteoClientAdapter(builder, baseUrl, baseUrl);

            // Must fail fast (well under the 10s server hold) rather than hang.
            assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                    assertThrows(RuntimeException.class,
                            () -> adapter.fetchCurrent(BEACH_ID, LATITUDE, LONGITUDE)));
        }
    }
}
