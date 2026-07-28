package com.nortadas.web.controller;

import com.nortadas.application.usecase.BeachStatusView;
import com.nortadas.application.usecase.GetBeachListUseCase;
import com.nortadas.application.usecase.PageResult;
import com.nortadas.web.dto.BeachResponse;
import com.nortadas.web.error.InvalidPaginationException;
import com.nortadas.web.mapper.BeachDtoMapper;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound HTTP adapter for the beach catalogue (US011). GRASP controller only:
 * validates the request, delegates to {@link GetBeachListUseCase}, and maps the
 * result to HAL via {@link BeachDtoMapper} — no business logic
 * (docs/architecture.md §5, §6).
 */
@RestController
@RequestMapping("/api/beaches")
public class BeachController {

    /** Maximum page size accepted by the list endpoint, per the API contract. */
    static final int MAX_PAGE_SIZE = 100;

    private final GetBeachListUseCase getBeachListUseCase;
    private final BeachDtoMapper beachDtoMapper;

    public BeachController(GetBeachListUseCase getBeachListUseCase, BeachDtoMapper beachDtoMapper) {
        this.getBeachListUseCase = getBeachListUseCase;
        this.beachDtoMapper = beachDtoMapper;
    }

    /**
     * Returns a HAL page of beaches with their current Nortada status.
     *
     * @param page zero-based page index (must be {@code >= 0})
     * @param size page size (must be {@code 1..}{@value #MAX_PAGE_SIZE})
     * @return the HAL {@link PagedModel} of {@link BeachResponse} items
     * @throws InvalidPaginationException when {@code page} or {@code size} is out of range
     */
    @GetMapping
    public PagedModel<BeachResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new InvalidPaginationException("page must not be negative, got " + page + ".");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException(
                    "size must be between 1 and " + MAX_PAGE_SIZE + ", got " + size + ".");
        }

        PageResult<BeachStatusView> result = getBeachListUseCase.getBeaches(page, size);
        return beachDtoMapper.toListResponse(result);
    }
}
