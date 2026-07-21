package com.nortadas.domain.region;

import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.RegionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegionFactoryTest {

    @Test
    @DisplayName("create derives the region's identity from its name")
    void createDerivesIdentityFromName() {
        Region region = RegionFactory.create(new Name("Norte"));

        assertEquals(RegionId.of("NOR"), region.getRegionId());
        assertEquals(new Name("Norte"), region.getName());
    }

    @Test
    @DisplayName("rehydrate keeps the given identity")
    void rehydrateKeepsGivenIdentity() {
        RegionId id = RegionId.of("CEN");

        Region region = RegionFactory.rehydrate(id, new Name("Centro"));

        assertEquals(id, region.getRegionId());
        assertEquals(new Name("Centro"), region.getName());
    }
}
