package com.nortadas.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NameTest {

    // --- accepted values -------------------------------------------------

    @ParameterizedTest(name = "accepts \"{0}\"")
    @ValueSource(strings = {
            "Praia de São Jacinto",   // accented letters + spaces
            "Costa Nova",             // plain letters + space
            "Vila Nova de Mil-Fontes",// hyphen
            "Praia d'El Rey",         // apostrophe
            "ab"                      // exact minimum length (2)
    })
    void acceptsValidNames(String value) {
        assertEquals(value, new Name(value).getValue());
    }

    @Test
    @DisplayName("accepts a name of exactly 80 characters (upper boundary)")
    void acceptsNameOfExactlyEightyCharacters() {
        String eighty = "a".repeat(80);
        assertEquals(eighty, new Name(eighty).getValue());
    }

    // --- blank / null ----------------------------------------------------

    @Test
    void rejectsNull() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new Name(null));
        assertEquals("Name cannot be blank!", ex.getMessage());
    }

    @ParameterizedTest(name = "rejects blank value \"{0}\"")
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void rejectsBlankValues(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Name(value));
    }

    // --- length boundaries -----------------------------------------------

    @Test
    @DisplayName("rejects a single character (just below minimum length)")
    void rejectsSingleCharacter() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new Name("a"));
        assertEquals("Name must have between 2 and 80 characters!", ex.getMessage());
    }

    @Test
    @DisplayName("rejects 81 characters (just above maximum length)")
    void rejectsEightyOneCharacters() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new Name("a".repeat(81)));
        assertEquals("Name must have between 2 and 80 characters!", ex.getMessage());
    }

    // --- character set ---------------------------------------------------

    @ParameterizedTest(name = "rejects special characters in \"{0}\"")
    @ValueSource(strings = {
            "Praia1",          // digit
            "Praia!",          // punctuation
            "Praia<script>",   // markup
            "Praia_Norte",     // underscore
            "Praia.Norte",     // dot
            "Praia, Norte",    // comma
            "Praia@Norte"      // symbol
    })
    void rejectsSpecialCharacters(String value) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new Name(value));
        assertEquals("Name cannot contain special characters!", ex.getMessage());
    }

    // --- equals / hashCode / toString ------------------------------------

    @Test
    void equalsIsReflexive() {
        Name name = new Name("Costa Nova");
        assertEquals(name, name);
    }

    @Test
    void equalNamesWithSameValueAndHashCodesMatch() {
        assertEquals(new Name("Costa Nova"), new Name("Costa Nova"));
        assertEquals(new Name("Costa Nova").hashCode(), new Name("Costa Nova").hashCode());
    }

    @Test
    void namesWithDifferentValuesAreNotEqual() {
        assertNotEquals(new Name("Costa Nova"), new Name("Barra"));
    }

    @Test
    void isNotEqualToNullOrOtherType() {
        Name name = new Name("Costa Nova");
        assertNotEquals(name, null);
        assertNotEquals(name, "Costa Nova");
    }

    @Test
    void toStringReturnsRawValue() {
        assertTrue(new Name("Costa Nova").toString().equals("Costa Nova"));
    }
}
