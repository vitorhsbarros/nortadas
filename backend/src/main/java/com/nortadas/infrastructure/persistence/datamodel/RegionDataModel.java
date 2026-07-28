package com.nortadas.infrastructure.persistence.datamodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA data model for the {@code region} table (docs/architecture.md §3, §8): a
 * Hibernate-shaped row, kept separate from the pure-domain
 * {@link com.nortadas.domain.region.Region} and mapped at the boundary by
 * {@link com.nortadas.infrastructure.persistence.mapper.RegionMapper}.
 *
 * <p>{@code id} stores the domain {@code RegionId} string form (a short
 * name-derived code, e.g. {@code NOR}); the schema owns its constraints
 * (Flyway), so this data model only mirrors the columns for Hibernate's
 * {@code validate}.
 */
@Entity
@Table(name = "region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RegionDataModel {

    @Id
    @Column(name = "id", nullable = false, length = 40)
    private String id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;
}
