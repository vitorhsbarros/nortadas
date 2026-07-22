package com.nortadas.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;

/**
 * Applies connect/read timeouts to the application's {@link org.springframework.web.client.RestClient}
 * HTTP calls (issue #50, docs/architecture.md §1 — wiring lives in {@code config}).
 *
 * <p>Live testing of the hourly weather fetch (US009) surfaced two ways the
 * external call could misbehave: an outright error (already handled per-beach in
 * {@code FetchWeatherUseCase}) and — the gap this closes — a response that is
 * slow rather than failing. With no timeout, one unresponsive beach would hang
 * the whole sequential sweep indefinitely. A {@link RestClientCustomizer} is the
 * idiomatic Spring Boot seam for this: it is applied to the auto-configured
 * {@code RestClient.Builder} that {@link com.nortadas.infrastructure.weather.OpenMeteoClientAdapter}
 * injects, so both the forecast and marine clients it builds inherit the
 * timeouts. A timeout then surfaces as a {@code RuntimeException} that the
 * use case's existing per-beach catch isolates, moving on to the next beach.
 *
 * <p>Timeouts are configurable ({@code nortadas.weather.open-meteo.connect-timeout}
 * and {@code read-timeout}) and kept short — this is an unattended background
 * job, so one bad beach should cost seconds, not minutes. Open-Meteo is the only
 * outbound HTTP client today; if an unrelated one is added later that needs a
 * different policy, scope the timeouts per client rather than globally here.
 */
@Configuration
public class OpenMeteoHttpClientConfig {

    @Bean
    public RestClientCustomizer openMeteoTimeoutCustomizer(
            @Value("${nortadas.weather.open-meteo.connect-timeout}") Duration connectTimeout,
            @Value("${nortadas.weather.open-meteo.read-timeout}") Duration readTimeout) {

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect()
                .build(ClientHttpRequestFactorySettings.defaults()
                        .withConnectTimeout(connectTimeout)
                        .withReadTimeout(readTimeout));

        return builder -> builder.requestFactory(requestFactory);
    }
}
