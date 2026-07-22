package com.nortadas.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherReadingIdTest {

    @Test
    void wrapsTheGivenUuid() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, new WeatherReadingId(uuid).getValue());
    }

    @Test
    void rejectsNullUuid() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new WeatherReadingId(null));
        assertEquals("Weather reading id cannot be null!", ex.getMessage());
    }

    @Test
    @DisplayName("newId() generates a fresh identity every call")
    void newIdGeneratesDistinctIdentities() {
        WeatherReadingId first = WeatherReadingId.newId();
        WeatherReadingId second = WeatherReadingId.newId();
        assertNotNull(first.getValue());
        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("of() rehydrates from a canonical UUID string")
    void ofRehydratesFromString() {
        UUID uuid = UUID.randomUUID();
        assertEquals(new WeatherReadingId(uuid), WeatherReadingId.of(uuid.toString()));
    }

    @Test
    void ofRejectsNull() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> WeatherReadingId.of(null));
        assertEquals("Weather reading id cannot be null!", ex.getMessage());
    }

    @Test
    void ofRejectsMalformedUuid() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> WeatherReadingId.of("not-a-uuid"));
        assertEquals("Weather reading id must be a valid UUID!", ex.getMessage());
    }

    @Test
    void equalsIsReflexive() {
        WeatherReadingId id = WeatherReadingId.newId();
        assertEquals(id, id);
    }

    @Test
    void idsWrappingSameUuidAreEqualWithMatchingHashCodes() {
        UUID uuid = UUID.randomUUID();
        assertEquals(new WeatherReadingId(uuid), new WeatherReadingId(uuid));
        assertEquals(new WeatherReadingId(uuid).hashCode(), new WeatherReadingId(uuid).hashCode());
    }

    @Test
    void idsWrappingDifferentUuidsAreNotEqual() {
        assertNotEquals(new WeatherReadingId(UUID.randomUUID()), new WeatherReadingId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("is not equal to null, another type, or a BeachId")
    void isNotEqualToNullOrOtherType() {
        UUID uuid = UUID.randomUUID();
        WeatherReadingId id = new WeatherReadingId(uuid);
        assertNotEquals(null, id);
        assertNotEquals(uuid, id);
        assertNotEquals(id, new BeachId(uuid));
    }

    @Test
    void toStringRendersTheUuid() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), new WeatherReadingId(uuid).toString());
    }
}
