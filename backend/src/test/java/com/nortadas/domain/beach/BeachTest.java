package com.nortadas.domain.beach;

import com.nortadas.domain.municipality.Municipality;
import com.nortadas.domain.municipality.MunicipalityFactory;
import com.nortadas.domain.region.Region;
import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeachTest {

    private static final Name NAME = new Name("Praia da Barra");
    private static final Latitude LATITUDE = new Latitude(40.6405);
    private static final Longitude LONGITUDE = new Longitude(-8.7527);
    private static final Region REGION = RegionFactory.create(new Name("Centro"));
    private static final Municipality MUNICIPALITY =
            MunicipalityFactory.create(MunicipalityId.of("1609"), new Name("Viana do Castelo"), REGION);

    private static Beach barra() {
        return new Beach(NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
    }

    // --- creation --------------------------------------------------------

    @Test
    @DisplayName("creating a beach generates its own identity and keeps all attributes")
    void createConstructorGeneratesIdentityAndKeepsAttributes() {
        Beach beach = barra();
        assertNotNull(beach.getBeachId());
        assertEquals(NAME, beach.getName());
        assertEquals(LATITUDE, beach.getLatitude());
        assertEquals(LONGITUDE, beach.getLongitude());
        assertEquals(MUNICIPALITY, beach.getMunicipality());
        assertEquals(REGION, beach.getRegion());
    }

    @Test
    void createConstructorGeneratesDistinctIdentities() {
        assertNotEquals(barra(), barra());
    }

    @Test
    @DisplayName("rehydration constructor keeps the given identity")
    void rehydrationConstructorKeepsGivenIdentity() {
        BeachId id = BeachId.newId();
        Beach beach = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        assertEquals(id, beach.getBeachId());
    }

    // --- invariants ------------------------------------------------------

    @Test
    void rejectsNullId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Beach(null, NAME, LATITUDE, LONGITUDE, MUNICIPALITY));
        assertEquals("Beach id cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Beach(BeachId.newId(), null, LATITUDE, LONGITUDE, MUNICIPALITY));
        assertEquals("Beach name cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullLatitude() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Beach(BeachId.newId(), NAME, null, LONGITUDE, MUNICIPALITY));
        assertEquals("Beach latitude cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullLongitude() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Beach(BeachId.newId(), NAME, LATITUDE, null, MUNICIPALITY));
        assertEquals("Beach longitude cannot be null!", ex.getMessage());
    }

    @Test
    void rejectsNullMunicipality() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Beach(BeachId.newId(), NAME, LATITUDE, LONGITUDE, null));
        assertEquals("Beach municipality cannot be null!", ex.getMessage());
    }

    // --- identity-based equality -----------------------------------------

    @Test
    void equalsIsReflexive() {
        Beach beach = barra();
        assertEquals(beach, beach);
    }

    @Test
    @DisplayName("beaches with the same id are equal even with different attributes")
    void beachesWithSameIdAreEqualRegardlessOfAttributes() {
        BeachId id = BeachId.newId();
        Beach first = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        Region norte = RegionFactory.create(new Name("Norte"));
        Municipality otherMunicipality =
                MunicipalityFactory.create(MunicipalityId.of("0107"), new Name("Espinho"), norte);
        Beach second = new Beach(id, new Name("Costa Nova"),
                new Latitude(40.6096), new Longitude(-8.7538), otherMunicipality);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("beaches with different ids are not equal even with identical attributes")
    void beachesWithDifferentIdsAreNotEqualDespiteSameAttributes() {
        assertNotEquals(
                new Beach(BeachId.newId(), NAME, LATITUDE, LONGITUDE, MUNICIPALITY),
                new Beach(BeachId.newId(), NAME, LATITUDE, LONGITUDE, MUNICIPALITY));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        Beach beach = barra();
        assertNotEquals(beach, null);
        assertNotEquals(beach, "Praia da Barra");
    }

    // --- attribute-based sameAs ------------------------------------------

    @Test
    @DisplayName("same id + identical attributes => equals AND sameAs")
    void sameIdIdenticalAttributesIsEqualAndSameAs() {
        BeachId id = BeachId.newId();
        Beach first = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        Beach second = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        assertEquals(first, second);
        assertTrue(first.sameAs(second));
    }

    @Test
    @DisplayName("same id + different name => equals but NOT sameAs")
    void sameIdDifferentNameIsEqualButNotSameAs() {
        BeachId id = BeachId.newId();
        Beach first = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        Beach second = new Beach(id, new Name("Costa Nova"), LATITUDE, LONGITUDE, MUNICIPALITY);
        assertEquals(first, second);
        assertFalse(first.sameAs(second));
    }

    @Test
    @DisplayName("same id + different latitude => NOT sameAs")
    void sameIdDifferentLatitudeIsNotSameAs() {
        BeachId id = BeachId.newId();
        Beach first = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        Beach second = new Beach(id, NAME, new Latitude(41.0), LONGITUDE, MUNICIPALITY);
        assertFalse(first.sameAs(second));
    }

    @Test
    @DisplayName("same id + different longitude => NOT sameAs")
    void sameIdDifferentLongitudeIsNotSameAs() {
        BeachId id = BeachId.newId();
        Beach first = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        Beach second = new Beach(id, NAME, LATITUDE, new Longitude(-9.0), MUNICIPALITY);
        assertFalse(first.sameAs(second));
    }

    @Test
    @DisplayName("same id + different municipality => NOT sameAs")
    void sameIdDifferentMunicipalityIsNotSameAs() {
        BeachId id = BeachId.newId();
        Beach first = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        Municipality otherMunicipality = MunicipalityFactory.create(
                MunicipalityId.of("0107"), new Name("Espinho"), RegionFactory.create(new Name("Norte")));
        Beach second = new Beach(id, NAME, LATITUDE, LONGITUDE, otherMunicipality);
        assertFalse(first.sameAs(second));
    }

    @Test
    @DisplayName("different id + identical attributes => neither equals nor sameAs")
    void differentIdIdenticalAttributesIsNeitherEqualNorSameAs() {
        Beach first = new Beach(BeachId.newId(), NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        Beach second = new Beach(BeachId.newId(), NAME, LATITUDE, LONGITUDE, MUNICIPALITY);
        assertNotEquals(first, second);
        assertFalse(first.sameAs(second));
    }

    @Test
    void sameAsNullIsFalse() {
        assertFalse(barra().sameAs(null));
    }

    // --- toString --------------------------------------------------------

    @Test
    void toStringContainsIdentityAndAttributes() {
        BeachId id = BeachId.newId();
        String rendered = new Beach(id, NAME, LATITUDE, LONGITUDE, MUNICIPALITY).toString();
        assertTrue(rendered.contains(id.toString()));
        assertTrue(rendered.contains("Praia da Barra"));
        assertTrue(rendered.contains("40.6405"));
        assertTrue(rendered.contains("-8.7527"));
        assertTrue(rendered.contains("Viana do Castelo"));
    }
}
