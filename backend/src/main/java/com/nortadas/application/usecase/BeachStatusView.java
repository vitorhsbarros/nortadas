package com.nortadas.application.usecase;

import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.weatherreading.WeatherReading;
import java.util.Optional;

/**
 * Application-layer result of composing a {@link Beach} with its current Nortada
 * status (docs/architecture.md §4; OOD {@code BeachStatusView}). It is what the
 * beach-query use cases ({@code GetBeachListUseCase}, {@code GetBeachDetailUseCase})
 * return to the web layer, which maps it to a HAL DTO.
 *
 * <p>The {@code latestReading} is optional: a beach that has never been fetched
 * yet (or whose readings have all aged out of the retention window) has no
 * reading, in which case the {@code status} is {@link NortadaStatus#NONE}. The
 * detection service is never asked to grade a missing reading — that decision is
 * made in the use case, so this view only carries the already-decided result.
 *
 * @param beach         the beach; never {@code null}
 * @param latestReading the most recent stored reading, if any
 * @param status        the Nortada status derived from {@code latestReading},
 *                      or {@link NortadaStatus#NONE} when there is none; never
 *                      {@code null}
 */
public record BeachStatusView(Beach beach, Optional<WeatherReading> latestReading, NortadaStatus status) {

    public BeachStatusView {
        if (beach == null) {
            throw new IllegalArgumentException("BeachStatusView beach cannot be null!");
        }
        if (latestReading == null) {
            throw new IllegalArgumentException("BeachStatusView latestReading cannot be null (use Optional.empty())!");
        }
        if (status == null) {
            throw new IllegalArgumentException("BeachStatusView status cannot be null!");
        }
    }
}
