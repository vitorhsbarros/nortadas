package com.nortadas.web.mapper;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.nortadas.application.usecase.BeachStatusView;
import com.nortadas.application.usecase.PageResult;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.weatherreading.WeatherReading;
import com.nortadas.web.controller.BeachController;
import com.nortadas.web.dto.BeachResponse;
import com.nortadas.web.dto.WeatherReadingResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

/**
 * Explicit {@code BeachStatusView} → HAL DTO mapper (Pure Fabrication;
 * docs/architecture.md §3, §6): the only place the application-layer view is
 * translated into the wire format returned by {@code BeachController}. Domain
 * objects are never serialized directly.
 */
@Component
public class BeachDtoMapper {

    /**
     * Maps a single {@link BeachStatusView} to its HAL item, with {@code self}
     * (the beach's detail URI {@code /api/beaches/{id}}) and {@code collection}
     * ({@code /api/beaches}) links.
     *
     * <p>The {@code self} link is built structurally from
     * {@link BeachController} rather than via a controller method reference,
     * because the detail endpoint ({@code GET /api/beaches/{id}}) does not exist
     * yet (US012/#17): {@code linkTo(BeachController.class).slash(id)} yields the
     * same {@code /api/beaches/{id}} URI without depending on a {@code detail(...)}
     * method.
     */
    public BeachResponse toListItem(BeachStatusView view) {
        Beach beach = view.beach();
        UUID id = beach.getBeachId().getValue();
        String condition = view.latestReading()
                .map(r -> r.getWeatherCondition().name())
                .orElse(null);
        BeachResponse response = new BeachResponse(
                id,
                beach.getName().getValue(),
                beach.getRegion().getName().getValue(),
                view.status().name(),
                condition);
        response.add(linkTo(BeachController.class).slash(id.toString()).withSelfRel());
        response.add(linkTo(BeachController.class).withRel(IanaLinkRelations.COLLECTION));
        return response;
    }

    /**
     * Maps a {@link BeachStatusView} to the HAL detail resource returned by
     * {@code GET /api/beaches/{id}} (US012): the same identity/name/region/status
     * as a list item, plus the optional {@code reading} block when the beach has a
     * latest stored reading, and {@code self} / {@code collection} links.
     *
     * <p>Unlike {@link #toListItem(BeachStatusView)}, the {@code self} link is
     * built from the now-existing {@link BeachController#detail(UUID)} method so
     * the URI stays coupled to the mapping rather than a hand-written path.
     */
    public BeachResponse toDetail(BeachStatusView view) {
        Beach beach = view.beach();
        UUID id = beach.getBeachId().getValue();

        WeatherReadingResponse reading = view.latestReading()
                .map(this::toReadingResponse)
                .orElse(null);
        String condition = view.latestReading()
                .map(r -> r.getWeatherCondition().name())
                .orElse(null);

        BeachResponse response = new BeachResponse(
                id,
                beach.getName().getValue(),
                beach.getRegion().getName().getValue(),
                view.status().name(),
                condition,
                reading);
        response.add(linkTo(methodOn(BeachController.class).detail(id)).withSelfRel());
        response.add(linkTo(BeachController.class).withRel(IanaLinkRelations.COLLECTION));
        return response;
    }

    private WeatherReadingResponse toReadingResponse(WeatherReading reading) {
        return new WeatherReadingResponse(
                reading.getWindSpeed().getKmPerHour(),
                reading.getWindDirection().getDegrees(),
                reading.getTemperatureCelsius(),
                reading.getWeatherCode().getValue(),
                reading.getFetchedAt().toString());
    }

    /**
     * Maps a {@link PageResult} of views to a {@link PagedModel} of HAL items with
     * HAL page metadata and pagination links ({@code self}, {@code first},
     * {@code prev} when present, {@code next} when present, {@code last}).
     */
    public PagedModel<BeachResponse> toListResponse(PageResult<BeachStatusView> page) {
        List<BeachResponse> items = page.content().stream()
                .map(this::toListItem)
                .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                page.pageSize(),
                page.pageNumber(),
                page.totalElements(),
                page.totalPages());

        PagedModel<BeachResponse> model = PagedModel.of(items, metadata);

        model.add(linkTo(methodOn(BeachController.class)
                .list(page.pageNumber(), page.pageSize())).withSelfRel());
        model.add(linkTo(methodOn(BeachController.class)
                .list(0, page.pageSize())).withRel(IanaLinkRelations.FIRST));
        if (page.hasPrevious()) {
            model.add(linkTo(methodOn(BeachController.class)
                    .list(page.pageNumber() - 1, page.pageSize())).withRel(IanaLinkRelations.PREV));
        }
        if (page.hasNext()) {
            model.add(linkTo(methodOn(BeachController.class)
                    .list(page.pageNumber() + 1, page.pageSize())).withRel(IanaLinkRelations.NEXT));
        }
        model.add(linkTo(methodOn(BeachController.class)
                .list(page.lastPageNumber(), page.pageSize())).withRel(IanaLinkRelations.LAST));

        return model;
    }
}
