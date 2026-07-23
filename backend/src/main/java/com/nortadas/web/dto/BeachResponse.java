package com.nortadas.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * HAL DTO for a single beach: the beach's identity, name, region, current
 * Nortada status, and derived weather condition, plus its own {@code _links}
 * ({@code self}, {@code collection}). Independent of both the domain
 * {@code Beach} and the JPA data model (docs/architecture.md §3), shaped for the
 * REST Level 3 HAL+JSON contract.
 *
 * <p>{@code @Relation(collectionRelation = "beaches")} makes {@code PagedModel}
 * embed a collection of these under {@code _embedded.beaches}.
 *
 * <p>The {@code weatherCondition} is the coarse, derived WMO weather category
 * (see {@code WeatherCondition}) carried on both the list and detail endpoints as
 * a top-level summary, mirroring {@code nortadaStatus}. It is {@code null} — and,
 * thanks to {@code @JsonInclude(NON_NULL)}, omitted from the JSON — when the beach
 * has no stored reading yet. The <em>raw</em> WMO code lives inside the
 * detail-only {@code reading} block, not here.
 *
 * <p>The optional {@code reading} block carries the beach's latest weather
 * reading. It is populated only by the detail endpoint
 * ({@code GET /api/beaches/{id}}, US012/#17); list items ({@code GET /api/beaches},
 * US011) leave it {@code null}. {@code @JsonInclude(NON_NULL)} keeps the key out of
 * the JSON entirely when absent, so list items serialize without it.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "beaches")
public class BeachResponse extends RepresentationModel<BeachResponse> {

    private final UUID id;
    private final String name;
    private final String region;
    private final String nortadaStatus;
    private final String weatherCondition;
    private final WeatherReadingResponse reading;

    /**
     * Constructs a beach without a {@code reading} block — used by the list
     * endpoint, whose items carry the derived {@code weatherCondition} summary but
     * never the raw reading. Pass {@code null} for {@code weatherCondition} when
     * the beach has no stored reading.
     */
    public BeachResponse(UUID id, String name, String region, String nortadaStatus, String weatherCondition) {
        this(id, name, region, nortadaStatus, weatherCondition, null);
    }

    /**
     * Constructs a beach with an optional {@code reading} block — used by the
     * detail endpoint. Pass {@code null} for {@code weatherCondition} and/or
     * {@code reading} when the beach has no stored reading.
     */
    public BeachResponse(UUID id, String name, String region, String nortadaStatus,
                         String weatherCondition, WeatherReadingResponse reading) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.nortadaStatus = nortadaStatus;
        this.weatherCondition = weatherCondition;
        this.reading = reading;
    }
}
