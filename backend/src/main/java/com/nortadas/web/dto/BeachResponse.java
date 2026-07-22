package com.nortadas.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * HAL DTO for a single beach: the beach's identity, name, region, and current
 * Nortada status, plus its own {@code _links} ({@code self}, {@code collection}).
 * Independent of both the domain {@code Beach} and the JPA data model
 * (docs/architecture.md §3), shaped for the REST Level 3 HAL+JSON contract.
 *
 * <p>{@code @Relation(collectionRelation = "beaches")} makes {@code PagedModel}
 * embed a collection of these under {@code _embedded.beaches}.
 *
 * <p>The optional {@code reading} block carries the beach's latest weather
 * reading. It is populated only by the detail endpoint
 * ({@code GET /api/beaches/{id}}, US012/#17); list items ({@code GET /api/beaches},
 * US011) leave it {@code null}. {@code @JsonInclude(NON_NULL)} keeps the key out of
 * the JSON entirely when absent, so list items serialize exactly as before.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "beaches")
public class BeachResponse extends RepresentationModel<BeachResponse> {

    private final UUID id;
    private final String name;
    private final String region;
    private final String nortadaStatus;
    private final WeatherReadingResponse reading;

    /**
     * Constructs a beach without a reading — used by the list endpoint, whose
     * items never carry weather data.
     */
    public BeachResponse(UUID id, String name, String region, String nortadaStatus) {
        this(id, name, region, nortadaStatus, null);
    }

    /**
     * Constructs a beach with an optional {@code reading} block — used by the
     * detail endpoint. Pass {@code null} for a beach that has no stored reading.
     */
    public BeachResponse(UUID id, String name, String region, String nortadaStatus, WeatherReadingResponse reading) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.nortadaStatus = nortadaStatus;
        this.reading = reading;
    }
}
