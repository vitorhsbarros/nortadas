package com.nortadas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * JPA data model for the {@code beach} table (docs/architecture.md §3, §8),
 * mapped to/from the pure-domain {@link com.nortadas.domain.beach.Beach} by
 * {@link com.nortadas.infrastructure.persistence.mapper.BeachMapper}.
 *
 * <p>The owning {@link RegionEntity} is a {@code @ManyToOne} association (a beach
 * always has exactly one region), fetched eagerly because mapping to a domain
 * {@code Beach} always needs it.
 */
@Entity
@Table(name = "beach")
public class BeachEntity {

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
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

    /** Required by JPA; not for application use. */
    protected BeachEntity() {
    }

    public BeachEntity(UUID id, String name, double latitude, double longitude, RegionEntity region) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public RegionEntity getRegion() {
        return region;
    }

    public void setRegion(RegionEntity region) {
        this.region = region;
    }
}
