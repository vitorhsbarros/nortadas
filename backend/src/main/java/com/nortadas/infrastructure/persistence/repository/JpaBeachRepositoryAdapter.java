package com.nortadas.infrastructure.persistence.repository;

import com.nortadas.application.port.BeachRepositoryPort;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.infrastructure.persistence.mapper.BeachMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing {@link BeachRepositoryPort} over Spring Data JPA
 * (Repository + Adapter; docs/architecture.md §7, §8). Translates persisted
 * {@link com.nortadas.infrastructure.persistence.datamodel.BeachDataModel} rows into
 * domain {@link Beach} objects via {@link BeachMapper}, keeping Spring Data types
 * from leaking past the {@code infrastructure} boundary.
 */
@Component
public class JpaBeachRepositoryAdapter implements BeachRepositoryPort {

    private final BeachJpaRepository jpaRepository;
    private final BeachMapper beachMapper;

    public JpaBeachRepositoryAdapter(BeachJpaRepository jpaRepository, BeachMapper beachMapper) {
        this.jpaRepository = jpaRepository;
        this.beachMapper = beachMapper;
    }

    @Override
    public List<Beach> findAll() {
        return jpaRepository.findAll().stream()
                .map(beachMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Beach> findById(BeachId beachId) {
        return jpaRepository.findById(beachId.getValue())
                .map(beachMapper::toDomain);
    }
}
