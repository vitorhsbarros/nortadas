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
 * docs/architecture.md §3, §6). Delegates the owning municipality to
 * {@link MunicipalityMapper}; rehydrating to the domain rebuilds every value
 * object through its validating constructor via {@link BeachFactory}.
 */
@Component
public class BeachMapper {

    private final MunicipalityMapper municipalityMapper;

    public BeachMapper(MunicipalityMapper municipalityMapper) {
        this.municipalityMapper = municipalityMapper;
    }

    public Beach toDomain(BeachDataModel dataModel) {
        return BeachFactory.rehydrate(
                new BeachId(dataModel.getId()),
                new Name(dataModel.getName()),
                new Latitude(dataModel.getLatitude()),
                new Longitude(dataModel.getLongitude()),
                municipalityMapper.toDomain(dataModel.getMunicipality()));
    }

    public BeachDataModel toDataModel(Beach beach) {
        return new BeachDataModel(
                beach.getBeachId().getValue(),
                beach.getName().getValue(),
                beach.getLatitude().getDegrees(),
                beach.getLongitude().getDegrees(),
                municipalityMapper.toDataModel(beach.getMunicipality()));
    }
}
