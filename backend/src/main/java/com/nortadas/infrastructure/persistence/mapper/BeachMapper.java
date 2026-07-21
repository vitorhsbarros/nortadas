package com.nortadas.infrastructure.persistence.mapper;

import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.beach.BeachFactory;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.infrastructure.persistence.datamodel.BeachDataModel;
import org.springframework.stereotype.Component;

/**
 * Explicit {@link Beach} ⇄ {@link BeachDataModel} mapper (Pure Fabrication;
 * docs/architecture.md §3, §6). Delegates the owning region to
 * {@link RegionMapper}; rehydrating to the domain rebuilds every value object
 * through its validating constructor via {@link BeachFactory}.
 */
@Component
public class BeachMapper {

    private final RegionMapper regionMapper;

    public BeachMapper(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    public Beach toDomain(BeachDataModel dataModel) {
        return BeachFactory.rehydrate(
                new BeachId(dataModel.getId()),
                new Name(dataModel.getName()),
                new Latitude(dataModel.getLatitude()),
                new Longitude(dataModel.getLongitude()),
                regionMapper.toDomain(dataModel.getRegion()));
    }

    public BeachDataModel toDataModel(Beach beach) {
        return new BeachDataModel(
                beach.getBeachId().getValue(),
                beach.getName().getValue(),
                beach.getLatitude().getDegrees(),
                beach.getLongitude().getDegrees(),
                regionMapper.toDataModel(beach.getRegion()));
    }
}
