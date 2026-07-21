package com.nortadas.domain.favourite;

import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.beach.BeachFactory;
import com.nortadas.domain.municipality.Municipality;
import com.nortadas.domain.municipality.MunicipalityFactory;
import com.nortadas.domain.region.Region;
import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FavouriteBeachesTest {

    private static final Region REGION = RegionFactory.create(new Name("Centro"));
    private static final Municipality MUNICIPALITY =
            MunicipalityFactory.create(MunicipalityId.of("1602"), new Name("Caminha"), REGION);

    private static Beach beach(String name) {
        return BeachFactory.create(new Name(name), new Latitude(40.6), new Longitude(-8.75), MUNICIPALITY);
    }

    private FavouriteBeaches favourites;
    private Beach barra;
    private Beach costaNova;

    @BeforeEach
    void setUp() {
        favourites = new FavouriteBeaches();
        barra = beach("Praia da Barra");
        costaNova = beach("Costa Nova");
    }

    // --- add -------------------------------------------------------------

    @Test
    void startsEmpty() {
        assertTrue(favourites.getBeaches().isEmpty());
    }

    @Test
    void addMakesTheBeachAFavourite() {
        favourites.add(barra);
        assertTrue(favourites.contains(barra));
        assertEquals(Set.of(barra), favourites.getBeaches());
    }

    @Test
    @DisplayName("adding the same beach twice is a no-op (no duplicates)")
    void addingSameBeachTwiceIsNoOp() {
        favourites.add(barra);
        favourites.add(barra);
        assertEquals(1, favourites.getBeaches().size());
    }

    @Test
    @DisplayName("a beach with the same id counts as the same favourite (identity equality)")
    void addingSameIdentityTwiceIsNoOp() {
        favourites.add(barra);
        Beach sameIdentity = BeachFactory.rehydrate(barra.getBeachId(), new Name("Renamed"),
                new Latitude(0.0), new Longitude(0.0), MUNICIPALITY);
        favourites.add(sameIdentity);
        assertEquals(1, favourites.getBeaches().size());
    }

    @Test
    void addRejectsNull() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> favourites.add(null));
        assertEquals("Cannot favourite a null beach!", ex.getMessage());
    }

    // --- remove ----------------------------------------------------------

    @Test
    void removeUnfavouritesTheBeach() {
        favourites.add(barra);
        favourites.remove(barra);
        assertFalse(favourites.contains(barra));
        assertTrue(favourites.getBeaches().isEmpty());
    }

    @Test
    @DisplayName("removing an absent beach is a no-op")
    void removingAbsentBeachIsNoOp() {
        favourites.add(barra);
        favourites.remove(costaNova);
        assertEquals(Set.of(barra), favourites.getBeaches());
    }

    @Test
    void removeRejectsNull() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> favourites.remove(null));
        assertEquals("Cannot unfavourite a null beach!", ex.getMessage());
    }

    // --- contains --------------------------------------------------------

    @Test
    void containsIsFalseForAbsentBeach() {
        favourites.add(barra);
        assertFalse(favourites.contains(costaNova));
    }

    @Test
    @DisplayName("contains(null) is false rather than throwing")
    void containsNullIsFalse() {
        assertFalse(favourites.contains(null));
    }

    // --- exposed collection ----------------------------------------------

    @Test
    @DisplayName("getBeaches preserves insertion order")
    void getBeachesPreservesInsertionOrder() {
        favourites.add(costaNova);
        favourites.add(barra);
        assertEquals(List.of(costaNova, barra), List.copyOf(favourites.getBeaches()));
    }

    @Test
    @DisplayName("the exposed set cannot be mutated by callers")
    void exposedSetIsUnmodifiable() {
        favourites.add(barra);
        Set<Beach> exposed = favourites.getBeaches();
        assertThrows(UnsupportedOperationException.class, () -> exposed.add(costaNova));
        assertThrows(UnsupportedOperationException.class, () -> exposed.remove(barra));
        assertThrows(UnsupportedOperationException.class, exposed::clear);
    }

    // --- toString --------------------------------------------------------

    @Test
    void toStringReportsTheCount() {
        favourites.add(barra);
        favourites.add(costaNova);
        assertEquals("FavouriteBeaches{count=2}", favourites.toString());
    }
}
