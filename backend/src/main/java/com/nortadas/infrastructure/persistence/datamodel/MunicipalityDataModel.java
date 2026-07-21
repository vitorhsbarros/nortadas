package com.nortadas.infrastructure.persistence.datamodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * JPA data model for the {@code municipality} table (docs/architecture.md §3,
 * §8): a Hibernate-shaped row, kept separate from the pure-domain
 * {@link com.nortadas.domain.municipality.Municipality} and mapped at the
 * boundary by
 * {@link com.nortadas.infrastructure.persistence.mapper.MunicipalityMapper}.
 *
 * <p>{@code id} stores the domain {@code MunicipalityId} string form (the
 * four-digit INE/DICOFRE code, e.g. {@code 0107}); the schema owns its
 * constraints (Flyway), so this data model only mirrors the columns for
 * Hibernate's {@code validate}.
 *
 * <p>The owning {@link RegionDataModel} is a {@code @ManyToOne} association (a
 * municipality always has exactly one region), fetched eagerly because mapping
 * to a domain {@code Municipality} always needs it.
 */
@Entity
@Table(name = "municipality")
@Getter
public class MunicipalityDataModel {

    @Id
    @Column(name = "id", nullable = false, length = 4)
    private String id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionDataModel region;

    /** Required by JPA; not for application use. */
    protected MunicipalityDataModel() {
    }

    public MunicipalityDataModel(String id, String name, RegionDataModel region) {
        this.id = id;
        this.name = name;
        this.region = region;
    }
}
