package com.nortadas.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegionIdTest {

    // --- code derivation (fromName) ---------------------------------------

    @Test
    @DisplayName("fromName derives an uppercase 3-letter code from the name")
    void fromNameDerivesThreeLetterCode() {
        RegionId id = RegionId.fromName(new Name("Norte"));
        assertEquals("NOR", id.getValue());
    }

    @Test
    @DisplayName("fromName strips accents when deriving the code")
    void fromNameStripsAccents() {
        RegionId id = RegionId.fromName(new Name("Açores"));
        assertEquals("ACO", id.getValue());
    }

    @Test
    @DisplayName("fromName uppercases a lower-case name")
    void fromNameUppercasesLowerCaseName() {
        RegionId id = RegionId.fromName(new Name("centro"));
        assertEquals("CEN", id.getValue());
    }

    @Test
    @DisplayName("fromName skips non-letters (spaces, apostrophes) when taking the first three letters")
    void fromNameSkipsNonLetters() {
        assertEquals("ONE", RegionId.fromName(new Name("O'Neil")).getValue());
        assertEquals("ABC", RegionId.fromName(new Name("A B C")).getValue());
    }

    @Test
    @DisplayName("fromName uses a shorter code when the name yields fewer than three letters")
    void fromNameUsesShorterCodeForShortNames() {
        assertEquals("AL", RegionId.fromName(new Name("Al")).getValue());
        assertEquals("A", RegionId.fromName(new Name("A-")).getValue());
    }

    @Test
    void fromNameRejectsNullName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> RegionId.fromName(null));
        assertEquals("Region id requires a region name!", ex.getMessage());
    }

    @Test
    @DisplayName("fromName is deterministic: the same name always yields the same id")
    void fromNameIsDeterministic() {
        RegionId first = RegionId.fromName(new Name("Norte"));
        RegionId second = RegionId.fromName(new Name("Norte"));
        assertEquals(first, second);
    }

    @Test
    @DisplayName("fromName yields different ids for names with different codes")
    void fromNameDiffersAcrossDistinctCodes() {
        assertNotEquals(RegionId.fromName(new Name("Norte")), RegionId.fromName(new Name("Centro")));
    }

    // --- rehydration (of) ------------------------------------------------

    @Test
    @DisplayName("of round-trips a value produced by fromName")
    void ofRoundTripsFromName() {
        RegionId original = RegionId.fromName(new Name("Lisboa"));
        RegionId rehydrated = RegionId.of(original.getValue());
        assertEquals(original, rehydrated);
        assertEquals(original.hashCode(), rehydrated.hashCode());
    }

    @Test
    void ofAcceptsAOneLetterCode() {
        assertEquals("A", RegionId.of("A").getValue());
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
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("nor"));   // lower-case
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("NORT")); // 4-letter code
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("NOR-1")); // stray suffix
        assertThrows(IllegalArgumentException.class, () -> RegionId.of("NO1"));  // digit
    }

    // --- equality / hashCode / toString ---------------------------------

    @Test
    void equalsIsReflexive() {
        RegionId id = RegionId.fromName(new Name("Norte"));
        assertEquals(id, id);
    }

    @Test
    void idsWithTheSameValueAreEqualWithMatchingHashCodes() {
        RegionId a = RegionId.of("NOR");
        RegionId b = RegionId.of("NOR");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void idsWithDifferentValuesAreNotEqual() {
        assertNotEquals(RegionId.of("NOR"), RegionId.of("CEN"));
    }

    @Test
    @DisplayName("is not equal to null, another type, or a BeachId")
    void isNotEqualToNullOrOtherType() {
        RegionId id = RegionId.of("NOR");
        assertNotEquals(id, null);
        assertNotEquals(id, "NOR");
        assertNotEquals(id, BeachId.newId());
    }

    @Test
    void toStringRendersTheValue() {
        RegionId id = RegionId.of("NOR");
        assertEquals("NOR", id.toString());
    }
}
