package com.nortadas.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeachIdTest {

    @Test
    void wrapsTheGivenUuid() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, new BeachId(uuid).getValue());
    }

    @Test
    void rejectsNullUuid() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new BeachId(null));
        assertEquals("Beach id cannot be null!", ex.getMessage());
    }

    @Test
    @DisplayName("newId() generates a fresh identity every call")
    void newIdGeneratesDistinctIdentities() {
        BeachId first = BeachId.newId();
        BeachId second = BeachId.newId();
        assertNotNull(first.getValue());
        assertNotEquals(first, second);
    }

    @Test
    void equalsIsReflexive() {
        BeachId id = BeachId.newId();
        assertEquals(id, id);
    }

    @Test
    void idsWrappingSameUuidAreEqualWithMatchingHashCodes() {
        UUID uuid = UUID.randomUUID();
        assertEquals(new BeachId(uuid), new BeachId(uuid));
        assertEquals(new BeachId(uuid).hashCode(), new BeachId(uuid).hashCode());
    }

    @Test
    void idsWrappingDifferentUuidsAreNotEqual() {
        assertNotEquals(new BeachId(UUID.randomUUID()), new BeachId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("is not equal to null, another type, or a RegionId")
    void isNotEqualToNullOrOtherType() {
        UUID uuid = UUID.randomUUID();
        BeachId id = new BeachId(uuid);
        assertNotEquals(null, id);
        assertNotEquals(uuid, id);
        assertNotEquals(id, RegionId.fromName(new Name("Norte")));
    }

    @Test
    void toStringRendersTheUuid() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), new BeachId(uuid).toString());
    }
}
