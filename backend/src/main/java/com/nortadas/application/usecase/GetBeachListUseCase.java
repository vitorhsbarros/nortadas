package com.nortadas.application.usecase;

import com.nortadas.application.port.BeachRepositoryPort;
import com.nortadas.application.port.WeatherReadingRepositoryPort;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.service.NortadaDetectionService;
import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.weatherreading.WeatherReading;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Use case behind {@code GET /api/beaches} (US011): returns a page of beaches,
 * each paired with its current Nortada status (Facade; docs/architecture.md §4,
 * §7, ADR-006). One coordinating method, {@link #getBeaches(int, int)}.
 *
 * <p>Ordering is by beach name ascending, giving a stable, deterministic page
 * order (the API contract's default {@code sort=name,asc}). The catalogue is a
 * small, fixed reference set (tens of beaches, already loaded in full every
 * hourly fetch), so paging is applied in memory over {@link BeachRepositoryPort#findAll()}
 * rather than pushed into the repository — this keeps the port framework-free
 * (no Spring Data {@code Pageable} leaking into the application layer) at a
 * negligible cost for this data size. If the catalogue ever grows large enough
 * to matter, add a paged port method and move the slicing into the adapter.
 *
 * <p>Status is derived per beach from its latest stored reading via
 * {@link NortadaDetectionService}. A beach with no reading yet grades to
 * {@link NortadaStatus#NONE} and the detection service is never called for it —
 * only the beaches actually on the requested page are graded, not the whole
 * catalogue.
 */
@Service
public class GetBeachListUseCase {

    private final BeachRepositoryPort beachRepository;
    private final WeatherReadingRepositoryPort weatherReadingRepository;
    private final NortadaDetectionService detectionService;

    public GetBeachListUseCase(BeachRepositoryPort beachRepository,
                               WeatherReadingRepositoryPort weatherReadingRepository,
                               NortadaDetectionService detectionService) {
        this.beachRepository = beachRepository;
        this.weatherReadingRepository = weatherReadingRepository;
        this.detectionService = detectionService;
    }

    /**
     * Returns the requested page of beaches with their current Nortada status.
     *
     * @param page zero-based page index (must be {@code >= 0})
     * @param size page size (must be {@code >= 1})
     * @return the page of {@link BeachStatusView}s; empty content when the page
     *         lies beyond the catalogue, never {@code null}
     */
    public PageResult<BeachStatusView> getBeaches(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative!");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be positive!");
        }

        List<Beach> all = beachRepository.findAll().stream()
                .sorted(Comparator.comparing(beach -> beach.getName().getValue()))
                .toList();

        int from = Math.min((int) Math.min((long) page * size, all.size()), all.size());
        int to = Math.min(from + size, all.size());

        List<BeachStatusView> content = all.subList(from, to).stream()
                .map(this::toStatusView)
                .toList();

        return new PageResult<>(content, page, size, all.size());
    }

    private BeachStatusView toStatusView(Beach beach) {
        Optional<WeatherReading> latest = weatherReadingRepository.findLatestByBeachId(beach.getBeachId());
        NortadaStatus status = latest.map(detectionService::detect).orElse(NortadaStatus.NONE);
        return new BeachStatusView(beach, latest, status);
    }
}
