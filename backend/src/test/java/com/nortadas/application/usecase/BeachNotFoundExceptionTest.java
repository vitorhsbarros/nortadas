package com.nortadas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.nortadas.domain.valueobject.BeachId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BeachNotFoundException} (US012): the message format and
 * the id accessors the web layer's problem-detail advice relies on.
 */
class BeachNotFoundExceptionTest {

    @Test
    @DisplayName("carries a message naming the missing id and exposes both id accessors")
    void carriesIdAndMessage() {
        UUID raw = UUID.fromString("00000000-0000-0000-0000-000000000000");
        BeachId id = new BeachId(raw);

        BeachNotFoundException ex = new BeachNotFoundException(id);

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("No beach exists with id " + raw);
        assertThat(ex.getBeachId()).isEqualTo(id);
        assertThat(ex.getBeachIdValue()).isEqualTo(raw);
    }
}
