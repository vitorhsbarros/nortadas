package com.nortadas.infrastructure.persistence.mapper;

import com.nortadas.domain.region.Region;
import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.RegionId;
import com.nortadas.infrastructure.persistence.datamodel.RegionDataModel;
import org.springframework.stereotype.Component;

/**
 * Explicit {@link Region} ⇄ {@link RegionDataModel} mapper (Pure Fabrication;
 * docs/architecture.md §3, §6). Rehydrating to the domain reconstructs the
 * value objects through {@link RegionFactory}, so a stored value that
 * violates a domain invariant is caught here rather than silently trusted.
 */
@Component
public class RegionMapper {

    public Region toDomain(RegionDataModel dataModel) {
        return RegionFactory.rehydrate(RegionId.of(dataModel.getId()), new Name(dataModel.getName()));
    }

    public RegionDataModel toDataModel(Region region) {
        return new RegionDataModel(region.getRegionId().getValue(), region.getName().getValue());
    }
}
