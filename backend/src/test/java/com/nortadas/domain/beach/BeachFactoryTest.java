package com.nortadas.domain.beach;

import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.region.Region;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BeachFactoryTest {

    private final Region region = RegionFactory.create(new Name("Norte"));

    @Test
    @DisplayName("create generates a fresh identity")
    void createGeneratesFreshIdentity() {
        Beach beach = BeachFactory.create(new Name("Praia de Moledo"), new Latitude(41.8397), new Longitude(-8.8747), region);

        assertNotNull(beach.getBeachId());
        assertEquals(new Name("Praia de Moledo"), beach.getName());
        assertEquals(region, beach.getRegion());
    }

    @Test
    @DisplayName("rehydrate keeps the given identity")
    void rehydrateKeepsGivenIdentity() {
        BeachId id = BeachId.newId();

        Beach beach = BeachFactory.rehydrate(
                id, new Name("Praia de Moledo"), new Latitude(41.8397), new Longitude(-8.8747), region);

        assertEquals(id, beach.getBeachId());
        assertEquals(new Name("Praia de Moledo"), beach.getName());
        assertEquals(region, beach.getRegion());
    }
}
