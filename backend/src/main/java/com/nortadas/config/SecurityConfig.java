package com.nortadas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TEMPORARY security posture for the US007 scaffolding phase.
 *
 * <p>Without this bean, Spring Security's auto-configuration would lock every
 * endpoint behind a generated password, which is not the intended posture for a
 * public read-only API. For now:
 *
 * <ul>
 *   <li>all requests are permitted — the Phase 1 endpoints (US011/US012) are
 *       public reads and no authenticated user stories exist yet;</li>
 *   <li>CSRF is disabled and sessions are stateless — this is a token-less JSON
 *       API with no browser session to protect at this stage.</li>
 * </ul>
 *
 * <p>Revisit when user-facing stories (favourites, comments, votes) introduce
 * authentication and authorization requirements.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
