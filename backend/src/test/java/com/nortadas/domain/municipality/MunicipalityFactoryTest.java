package com.nortadas.domain.municipality;

import com.nortadas.domain.region.Region;
import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MunicipalityFactoryTest {

    @Test
    @DisplayName("create delegates to the constructor, keeping all three attributes")
    void createDelegatesToConstructor() {
        MunicipalityId id = MunicipalityId.of("1602");
        Name name = new Name("Caminha");
        Region region = RegionFactory.create(new Name("Norte"));

        Municipality municipality = MunicipalityFactory.create(id, name, region);

        assertEquals(id, municipality.getMunicipalityId());
        assertEquals(name, municipality.getName());
        assertEquals(region, municipality.getRegion());
    }
}
