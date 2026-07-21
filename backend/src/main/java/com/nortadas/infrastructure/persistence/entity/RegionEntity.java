package com.nortadas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA data model for the {@code region} table (docs/architecture.md §3, §8): a
 * Hibernate-shaped row, kept separate from the pure-domain
 * {@link com.nortadas.domain.region.Region} and mapped at the boundary by
 * {@link com.nortadas.infrastructure.persistence.mapper.RegionMapper}.
 *
 * <p>{@code id} stores the domain {@code RegionId} string form (a short
 * name-derived code, e.g. {@code NOR}); the schema owns its constraints
 * (Flyway), so this entity only mirrors the columns for Hibernate's
 * {@code validate}.
 */
@Entity
@Table(name = "region")
public class RegionEntity {

    @Id
    @Column(name = "id", nullable = false, length = 40)
    private String id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    /** Required by JPA; not for application use. */
    protected RegionEntity() {
    }

    public RegionEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
