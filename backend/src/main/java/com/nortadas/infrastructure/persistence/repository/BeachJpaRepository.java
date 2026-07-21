package com.nortadas.infrastructure.persistence.repository;

import com.nortadas.infrastructure.persistence.entity.BeachEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository over {@link BeachEntity}. Confined to
 * {@code infrastructure}; the application talks to
 * {@link com.nortadas.application.port.BeachRepositoryPort} instead
 * (docs/architecture.md §8).
 */
public interface BeachJpaRepository extends JpaRepository<BeachEntity, UUID> {
}
