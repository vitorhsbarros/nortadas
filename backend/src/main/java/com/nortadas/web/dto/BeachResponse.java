package com.nortadas.web.dto;

import java.util.UUID;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * HAL item DTO for a single beach in the {@code GET /api/beaches} listing (US011):
 * the beach's identity, name, region, and current Nortada status, plus its own
 * {@code _links} ({@code self}, {@code collection}). Independent of both the
 * domain {@code Beach} and the JPA data model (docs/architecture.md §3), shaped
 * for the REST Level 3 HAL+JSON contract.
 *
 * <p>{@code @Relation(collectionRelation = "beaches")} makes {@code PagedModel}
 * embed a collection of these under {@code _embedded.beaches}.
 *
 * <p>The detail view's weather reading is deliberately absent here — it belongs to
 * the detail endpoint ({@code GET /api/beaches/{id}}, US012/#17), not the list.
 */
@Getter
@Relation(collectionRelation = "beaches")
public class BeachResponse extends RepresentationModel<BeachResponse> {

    private final UUID id;
    private final String name;
    private final String region;
    private final String nortadaStatus;

    public BeachResponse(UUID id, String name, String region, String nortadaStatus) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.nortadaStatus = nortadaStatus;
    }
}
