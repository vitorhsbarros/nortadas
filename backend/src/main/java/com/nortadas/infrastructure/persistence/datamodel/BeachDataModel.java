package com.nortadas.infrastructure.persistence.datamodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA data model for the {@code beach} table (docs/architecture.md §3, §8),
 * mapped to/from the pure-domain {@link com.nortadas.domain.beach.Beach} by
 * {@link com.nortadas.infrastructure.persistence.mapper.BeachMapper}.
 *
 * <p>The owning {@link MunicipalityDataModel} is a {@code @ManyToOne}
 * association (a beach always has exactly one municipality), fetched eagerly
 * because mapping to a domain {@code Beach} always needs it.
 */
@Entity
@Table(name = "beach")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BeachDataModel {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "municipality_id", nullable = false)
    private MunicipalityDataModel municipality;
}
