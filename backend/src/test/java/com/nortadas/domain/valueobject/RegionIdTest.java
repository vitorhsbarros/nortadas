package com.nortadas.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionIdTest {

    private static final String UUID_PART = "550e8400-e29b-41d4-a716-446655440000";

    // --- prefix derivation (newId) --------------------------------------

    @Test
    @DisplayName("newId derives an uppercase 3-letter prefix from the name")
    void newIdDerivesThreeLetterPrefix() {
        RegionId id = RegionId.newId(new Name("Norte"));
        assertTrue(id.getValue().startsWith("NOR-"), id.getValue());
    }

    @Test
    @DisplayName("newId strips accents when deriving the prefix")
    void newIdStripsAccents() {
        RegionId id = RegionId.newId(new Name("Açores"));
        assertTrue(id.getValue().startsWith("ACO-"), id.getValue());
    }

    @Test
    @DisplayName("newId uppercases a lower-case name")
    void newIdUppercasesLowerCaseName() {
        RegionId id = RegionId.newId(new Name("centro"));
        assertTrue(id.getValue().startsWith("CEN-"), id.getValue());
    }

    @Test
    @DisplayName("newId skips non-letters (spaces, apostrophes) when taking the first three letters")
    void newIdSkipsNonLetters() {
        assertTrue(RegionId.newId(new Name("O'Neil")).getValue().startsWith("ONE-"));
        assertTrue(RegionId.newId(new Name("A B C")).getValue().startsWith("ABC-"));
    }

    @Test
    @DisplayName("newId uses a shorter prefix when the name yields fewer than three letters")
    void newIdUsesShorterPrefixForShortNames() {
        assertTrue(RegionId.newId(new Name("Al")).getValue().startsWith("AL-"));
        assertTrue(RegionId.newId(new Name("A-")).getValue().startsWith("A-"));
    }

    @Test
    void newIdRejectsNullName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> RegionId.newId(null));
        assertEquals("Region id requires a region name!", ex.getMessage());
    }

    @Test
    @DisplayName("newId produces the same prefix but distinct values for the same name")
    void newIdSamePrefixDistinctValues() {
        RegionId first = RegionId.newId(new Name("Norte"));
        RegionId second = RegionId.newId(new Name("Norte"));
        assertTrue(first.getValue().startsWith("NOR-"));
        assertTrue(second.getValue().startsWith("NOR-"));
        assertNotEquals(first, second);
    }

    // --- rehydration (of) ------------------------------------------------

    @Test
    @DisplayName("of round-trips a value produced by newId")
    void ofRoundTripsNewId() {
        RegionId original = RegionId.newId(new Name("Lisboa"));
        RegionId rehydrated = RegionId.of(original.getValue());
        assertEquals(original, rehydrated);
        assertEquals(original.hashCode(), rehydrated.hashCode());
    }

    @Test
    void ofAcceptsAOneLetterPrefix() {
        assertEquals("A-" + UUID_PART, RegionId.of("A-" + UUID_PART).getValue());
    }

    @Test
    void ofRejectsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> RegionId.of(null));
        assertEquals("Region id cannot be null!", ex.getMessage());
    }

    @Test
    @DisplayName("of rejects malformed values")
    void ofRejectsMalformedValues() {
        assertThrows(IllegalArgumentException.class, () -> RegionId.of(""));
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("nor-" + UUID_PART));     // lower-case prefix
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("NORT-" + UUID_PART));    // 4-letter prefix
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("-" + UUID_PART));        // missing prefix
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("NOR" + UUID_PART));      // missing hyphen
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("NOR-not-a-uuid"));       // bad uuid
        assertThrows(IllegalArgumentException.class,
                () -> RegionId.of("NOR-" + UUID_PART.toUpperCase()));                            // upper-case uuid
    }

    // --- equality / hashCode / toString ---------------------------------

    @Test
    void equalsIsReflexive() {
        RegionId id = RegionId.newId(new Name("Norte"));
        assertEquals(id, id);
    }

    @Test
    void idsWithTheSameValueAreEqualWithMatchingHashCodes() {
        RegionId a = RegionId.of("NOR-" + UUID_PART);
        RegionId b = RegionId.of("NOR-" + UUID_PART);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void idsWithDifferentValuesAreNotEqual() {
        assertNotEquals(RegionId.newId(new Name("Norte")), RegionId.newId(new Name("Centro")));
    }

    @Test
    @DisplayName("is not equal to null, another type, or a BeachId")
    void isNotEqualToNullOrOtherType() {
        RegionId id = RegionId.of("NOR-" + UUID_PART);
        assertNotEquals(id, null);
        assertNotEquals(id, "NOR-" + UUID_PART);
        assertNotEquals(id, BeachId.newId());
    }

    @Test
    void toStringRendersTheValue() {
        RegionId id = RegionId.of("NOR-" + UUID_PART);
        assertEquals("NOR-" + UUID_PART, id.toString());
    }
}
