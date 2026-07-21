package com.nortadas.infrastructure.persistence.mapper;

import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.infrastructure.persistence.entity.BeachEntity;
import org.springframework.stereotype.Component;

/**
 * Explicit {@link Beach} ⇄ {@link BeachEntity} mapper (Pure Fabrication;
 * docs/architecture.md §3, §6). Delegates the owning region to
 * {@link RegionMapper}; rehydrating to the domain rebuilds every value object
 * through its validating constructor.
 */
@Component
public class BeachMapper {

    private final RegionMapper regionMapper;

    public BeachMapper(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    public Beach toDomain(BeachEntity entity) {
        return new Beach(
                new BeachId(entity.getId()),
                new Name(entity.getName()),
                new Latitude(entity.getLatitude()),
                new Longitude(entity.getLongitude()),
                regionMapper.toDomain(entity.getRegion()));
    }

    public BeachEntity toEntity(Beach beach) {
        return new BeachEntity(
                beach.getBeachId().getValue(),
                beach.getName().getValue(),
                beach.getLatitude().getDegrees(),
                beach.getLongitude().getDegrees(),
                regionMapper.toEntity(beach.getRegion()));
    }
}
