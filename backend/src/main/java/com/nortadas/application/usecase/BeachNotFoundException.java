package com.nortadas.application.usecase;

import com.nortadas.domain.valueobject.BeachId;
import java.util.UUID;

/**
 * Thrown by {@link GetBeachDetailUseCase} when no beach exists for the requested
 * {@link BeachId}. It lives in the application layer because that is where it is
 * raised; the web layer translates it into an RFC-7807 {@code 404 Not Found}
 * (see {@code ApiExceptionHandler}, US012/#17).
 */
public class BeachNotFoundException extends RuntimeException {

    private final BeachId beachId;

    public BeachNotFoundException(BeachId id) {
        super("No beach exists with id " + id.getValue());
        this.beachId = id;
    }

    /** The identity that had no matching beach. */
    public BeachId getBeachId() {
        return beachId;
    }

    /** Convenience accessor for the raw id, for the web-layer problem detail. */
    public UUID getBeachIdValue() {
        return beachId.getValue();
    }
}
