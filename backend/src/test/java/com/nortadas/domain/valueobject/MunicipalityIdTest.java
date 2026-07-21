package com.nortadas.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MunicipalityIdTest {

    // --- rehydration (of) -------------------------------------------------

    @Test
    @DisplayName("of accepts a valid 4-digit code")
    void ofAcceptsAValidFourDigitCode() {
        assertEquals("1602", MunicipalityId.of("1602").getValue());
    }

    @Test
    @DisplayName("of keeps significant leading zeros")
    void ofKeepsLeadingZeros() {
        assertEquals("0107", MunicipalityId.of("0107").getValue());
    }

    @Test
    void ofRejectsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MunicipalityId.of(null));
        assertEquals("Municipality id cannot be null!", ex.getMessage());
    }

    @Test
    @DisplayName("of rejects a code that is too short")
    void ofRejectsTooShortCode() {
        assertThrows(IllegalArgumentException.class, () -> MunicipalityId.of("160"));
    }

    @Test
    @DisplayName("of rejects a code that is too long")
    void ofRejectsTooLongCode() {
        assertThrows(IllegalArgumentException.class, () -> MunicipalityId.of("16022"));
    }

    @Test
    @DisplayName("of rejects non-digit characters")
    void ofRejectsNonDigitCharacters() {
        assertThrows(IllegalArgumentException.class, () -> MunicipalityId.of("16A2"));
    }

    @Test
    @DisplayName("of rejects a blank/empty value")
    void ofRejectsBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> MunicipalityId.of(""));
        assertThrows(IllegalArgumentException.class, () -> MunicipalityId.of("    "));
    }

    // --- equality / hashCode / toString ------------------------------------

    @Test
    void equalsIsReflexive() {
        MunicipalityId id = MunicipalityId.of("1602");
        assertEquals(id, id);
    }

    @Test
    void idsWithTheSameValueAreEqualWithMatchingHashCodes() {
        MunicipalityId a = MunicipalityId.of("1602");
        MunicipalityId b = MunicipalityId.of("1602");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void idsWithDifferentValuesAreNotEqual() {
        assertNotEquals(MunicipalityId.of("1602"), MunicipalityId.of("0107"));
    }

    @Test
    @DisplayName("is not equal to null, another type, or a RegionId")
    void isNotEqualToNullOrOtherType() {
        MunicipalityId id = MunicipalityId.of("1602");
        assertNotEquals(id, null);
        assertNotEquals(id, "1602");
        assertNotEquals(id, RegionId.of("NOR"));
    }

    @Test
    void toStringRendersTheValue() {
        MunicipalityId id = MunicipalityId.of("1602");
        assertEquals("1602", id.toString());
    }
}
