package com.nortadas.domain.municipality;

import com.nortadas.domain.region.Region;
import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MunicipalityTest {

    private static final MunicipalityId ID = MunicipalityId.of("1602");
    private static final Name NAME = new Name("Caminha");
    private static final Region REGION = RegionFactory.create(new Name("Norte"));

    // --- creation ----------------------------------------------------------

    @Test
    @DisplayName("construction keeps all three attributes")
    void constructionKeepsAllAttributes() {
        Municipality municipality = new Municipality(ID, NAME, REGION);
        assertEquals(ID, municipality.getMunicipalityId());
        assertEquals(NAME, municipality.getName());
        assertEquals(REGION, municipality.getRegion());
    }

    // --- invariants ----------------------------------------------------------

    @Test
    void rejectsNullId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Municipality(null, NAME, REGION));
        assertEquals("Municipality id cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Municipality(ID, null, REGION));
        assertEquals("Municipality name cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullRegion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Municipality(ID, NAME, null));
        assertEquals("Municipality region cannot be null!", ex.getMessage());
    }

    // --- identity-based equality ---------------------------------------------

    @Test
    void equalsIsReflexive() {
        Municipality municipality = new Municipality(ID, NAME, REGION);
        assertEquals(municipality, municipality);
    }

    @Test
    @DisplayName("municipalities with the same id are equal even with different name or region")
    void municipalitiesWithSameIdAreEqualRegardlessOfAttributes() {
        Municipality first = new Municipality(ID, NAME, REGION);
        Region centro = RegionFactory.create(new Name("Centro"));
        Municipality second = new Municipality(ID, new Name("Espinho"), centro);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("municipalities with different ids are not equal even with identical attributes")
    void municipalitiesWithDifferentIdsAreNotEqualDespiteSameAttributes() {
        assertNotEquals(
                new Municipality(MunicipalityId.of("1602"), NAME, REGION),
                new Municipality(MunicipalityId.of("0107"), NAME, REGION));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        Municipality municipality = new Municipality(ID, NAME, REGION);
        assertNotEquals(municipality, null);
        assertNotEquals(municipality, "Caminha");
    }

    // --- attribute-based sameAs ----------------------------------------------

    @Test
    @DisplayName("same id + identical attributes => equals AND sameAs")
    void sameIdIdenticalAttributesIsEqualAndSameAs() {
        Municipality first = new Municipality(ID, NAME, REGION);
        Municipality second = new Municipality(ID, NAME, REGION);
        assertEquals(first, second);
        assertTrue(first.sameAs(second));
    }

    @Test
    @DisplayName("same id + different name => equals but NOT sameAs")
    void sameIdDifferentNameIsEqualButNotSameAs() {
        Municipality first = new Municipality(ID, NAME, REGION);
        Municipality second = new Municipality(ID, new Name("Espinho"), REGION);
        assertEquals(first, second);
        assertFalse(first.sameAs(second));
    }

    @Test
    @DisplayName("same id + different region => equals but NOT sameAs")
    void sameIdDifferentRegionIsEqualButNotSameAs() {
        Municipality first = new Municipality(ID, NAME, REGION);
        Municipality second = new Municipality(ID, NAME, RegionFactory.create(new Name("Centro")));
        assertEquals(first, second);
        assertFalse(first.sameAs(second));
    }

    @Test
    @DisplayName("different id + identical name/region => neither equals nor sameAs")
    void differentIdSameAttributesIsNeitherEqualNorSameAs() {
        Municipality first = new Municipality(MunicipalityId.of("1602"), NAME, REGION);
        Municipality second = new Municipality(MunicipalityId.of("0107"), NAME, REGION);
        assertNotEquals(first, second);
        assertFalse(first.sameAs(second));
    }

    @Test
    void sameAsNullIsFalse() {
        assertFalse(new Municipality(ID, NAME, REGION).sameAs(null));
    }

    // --- toString --------------------------------------------------------

    @Test
    void toStringContainsIdAndName() {
        String rendered = new Municipality(ID, NAME, REGION).toString();
        assertTrue(rendered.contains(ID.toString()));
        assertTrue(rendered.contains("Caminha"));
    }
}
