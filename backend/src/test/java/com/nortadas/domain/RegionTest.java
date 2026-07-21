package com.nortadas.domain;

import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.RegionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionTest {

    // --- creation --------------------------------------------------------

    @Test
    @DisplayName("creating a region generates its own identity")
    void createConstructorGeneratesIdentity() {
        Region region = new Region(new Name("Norte"));
        assertNotNull(region.getRegionId());
        assertEquals(new Name("Norte"), region.getName());
    }

    @Test
    void createConstructorGeneratesDistinctIdentities() {
        assertNotEquals(new Region(new Name("Norte")), new Region(new Name("Norte")));
    }

    @Test
    @DisplayName("rehydration constructor keeps the given identity")
    void rehydrationConstructorKeepsGivenIdentity() {
        RegionId id = RegionId.newId(new Name("Centro"));
        Region region = new Region(id, new Name("Centro"));
        assertEquals(id, region.getRegionId());
        assertEquals(new Name("Centro"), region.getName());
    }

    // --- invariants ------------------------------------------------------

    @Test
    void rejectsNullId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Region(null, new Name("Norte")));
        assertEquals("Region id cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Region(RegionId.newId(new Name("Norte")), null));
        assertEquals("Region name cannot be null!", ex.getMessage());
    }

    @Test
    void createConstructorAlsoRejectsNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Region(null));
    }

    // --- identity-based equality -----------------------------------------

    @Test
    void equalsIsReflexive() {
        Region region = new Region(new Name("Norte"));
        assertEquals(region, region);
    }

    @Test
    @DisplayName("regions with the same id are equal even with different names")
    void regionsWithSameIdAreEqualRegardlessOfName() {
        RegionId id = RegionId.newId(new Name("Norte"));
        Region first = new Region(id, new Name("Norte"));
        Region second = new Region(id, new Name("Algarve"));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("regions with different ids are not equal even with the same name")
    void regionsWithDifferentIdsAreNotEqualDespiteSameName() {
        assertNotEquals(
                new Region(RegionId.newId(new Name("Norte")), new Name("Norte")),
                new Region(RegionId.newId(new Name("Norte")), new Name("Norte")));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        Region region = new Region(new Name("Norte"));
        assertNotEquals(region, null);
        assertNotEquals(region, "Norte");
    }

    // --- toString --------------------------------------------------------

    @Test
    void toStringContainsIdAndName() {
        RegionId id = RegionId.newId(new Name("Norte"));
        String rendered = new Region(id, new Name("Norte")).toString();
        assertTrue(rendered.contains(id.toString()));
        assertTrue(rendered.contains("Norte"));
    }
}
