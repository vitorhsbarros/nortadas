package com.nortadas.application.usecase;

import com.nortadas.application.port.BeachRepositoryPort;
import com.nortadas.application.port.WeatherReadingRepositoryPort;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.service.NortadaDetectionService;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.weatherreading.WeatherReading;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Use case behind {@code GET /api/beaches/{id}} (US012): returns a single beach
 * paired with its current Nortada status and its latest stored reading (Facade;
 * docs/architecture.md §4, §7, ADR-006). One coordinating method,
 * {@link #getBeach(BeachId)}.
 *
 * <p>Status is derived from the beach's latest stored reading via
 * {@link NortadaDetectionService}, mirroring {@link GetBeachListUseCase}: a beach
 * with no reading yet grades to {@link NortadaStatus#NONE} and the detection
 * service is never called for it. An unknown id raises
 * {@link BeachNotFoundException}, which the web layer maps to a {@code 404}.
 */
@Service
public class GetBeachDetailUseCase {

    private final BeachRepositoryPort beachRepository;
    private final WeatherReadingRepositoryPort weatherReadingRepository;
    private final NortadaDetectionService detectionService;

    public GetBeachDetailUseCase(BeachRepositoryPort beachRepository,
                                 WeatherReadingRepositoryPort weatherReadingRepository,
                                 NortadaDetectionService detectionService) {
        this.beachRepository = beachRepository;
        this.weatherReadingRepository = weatherReadingRepository;
        this.detectionService = detectionService;
    }

    /**
     * Returns the beach with the given id, its latest reading (if any), and the
     * Nortada status derived from that reading.
     *
     * @param id the beach identity to look up; never {@code null}
     * @return the composed {@link BeachStatusView}; never {@code null}
     * @throws BeachNotFoundException when no beach exists with that id
     */
    public BeachStatusView getBeach(BeachId id) {
        Beach beach = beachRepository.findById(id)
                .orElseThrow(() -> new BeachNotFoundException(id));

        Optional<WeatherReading> latest = weatherReadingRepository.findLatestByBeachId(beach.getBeachId());
        NortadaStatus status = latest.map(detectionService::detect).orElse(NortadaStatus.NONE);
        return new BeachStatusView(beach, latest, status);
    }
}
