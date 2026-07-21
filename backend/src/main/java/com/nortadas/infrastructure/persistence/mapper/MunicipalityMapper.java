package com.nortadas.infrastructure.persistence.mapper;

import com.nortadas.domain.municipality.Municipality;
import com.nortadas.domain.municipality.MunicipalityFactory;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.infrastructure.persistence.datamodel.MunicipalityDataModel;
import org.springframework.stereotype.Component;

/**
 * Explicit {@link Municipality} ⇄ {@link MunicipalityDataModel} mapper (Pure
 * Fabrication; docs/architecture.md §3, §6). Delegates the owning region to
 * {@link RegionMapper}; rehydrating to the domain reconstructs the value
 * objects through {@link MunicipalityFactory}, so a stored value that violates
 * a domain invariant is caught here rather than silently trusted.
 */
@Component
public class MunicipalityMapper {

    private final RegionMapper regionMapper;

    public MunicipalityMapper(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    public Municipality toDomain(MunicipalityDataModel dataModel) {
        return MunicipalityFactory.create(
                MunicipalityId.of(dataModel.getId()),
                new Name(dataModel.getName()),
                regionMapper.toDomain(dataModel.getRegion()));
    }

    public MunicipalityDataModel toDataModel(Municipality municipality) {
        return new MunicipalityDataModel(
                municipality.getMunicipalityId().getValue(),
                municipality.getName().getValue(),
                regionMapper.toDataModel(municipality.getRegion()));
    }
}
