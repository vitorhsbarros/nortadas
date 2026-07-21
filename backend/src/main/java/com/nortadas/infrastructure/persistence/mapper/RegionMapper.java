package com.nortadas.infrastructure.persistence.mapper;

import com.nortadas.domain.region.Region;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.RegionId;
import com.nortadas.infrastructure.persistence.entity.RegionEntity;
import org.springframework.stereotype.Component;

/**
 * Explicit {@link Region} ⇄ {@link RegionEntity} mapper (Pure Fabrication;
 * docs/architecture.md §3, §6). Rehydrating to the domain reconstructs the
 * value objects through their validating constructors, so a stored value that
 * violates a domain invariant is caught here rather than silently trusted.
 */
@Component
public class RegionMapper {

    public Region toDomain(RegionEntity entity) {
        return new Region(RegionId.of(entity.getId()), new Name(entity.getName()));
    }

    public RegionEntity toEntity(Region region) {
        return new RegionEntity(region.getRegionId().getValue(), region.getName().getValue());
    }
}
