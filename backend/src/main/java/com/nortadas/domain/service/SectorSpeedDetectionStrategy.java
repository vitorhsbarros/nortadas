package com.nortadas.domain.service;

import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.weatherreading.WeatherReading;

/**
 * The default US010 Nortada detection rule: a wind-direction sector gate,
 * followed by graded sustained-speed thresholds.
 *
 * <p><strong>Direction gate (first).</strong> A Nortada blows from the north.
 * The gating sector is <strong>N to NNW, 315°–45°</strong>, which wraps through
 * 0°/360°. Since {@link com.nortadas.domain.valueobject.WindDirection} stores
 * degrees in the half-open range {@code [0, 360)}, a reading is in-sector when
 * {@code degrees >= 315.0 || degrees <= 45.0}. Both endpoints are
 * <em>inclusive</em>: 315.0° and 45.0° are in-sector, while 314.999° and
 * 45.001° are off-sector. 0° (due North) is in-sector; 180° (South) is not.
 * Wind outside the sector is always {@link NortadaStatus#NONE}, whatever the
 * speed.
 *
 * <p><strong>Speed grading (within the sector).</strong> Sustained wind speed
 * in km/h grades the reading. Thresholds are lower-inclusive, upper-exclusive:
 * <ul>
 *   <li>{@code < 15} &rarr; {@link NortadaStatus#NONE}</li>
 *   <li>{@code [15, 25)} &rarr; {@link NortadaStatus#LIGHT}</li>
 *   <li>{@code [25, 40)} &rarr; {@link NortadaStatus#MODERATE}</li>
 *   <li>{@code [40, 55)} &rarr; {@link NortadaStatus#STRONG}</li>
 *   <li>{@code >= 55} &rarr; {@link NortadaStatus#SEVERE}</li>
 * </ul>
 *
 * <p>Detection runs year-round — there is no calendar/month-based exclusion.
 */
public final class SectorSpeedDetectionStrategy implements NortadaDetectionStrategy {

    /** Lower, inclusive edge of the gating sector (wraps up through 360°/0°). */
    private static final double SECTOR_LOWER_DEGREES = 315.0;
    /** Upper, inclusive edge of the gating sector (wraps down through 0°). */
    private static final double SECTOR_UPPER_DEGREES = 45.0;

    private static final double LIGHT_MIN_KM_H = 15.0;
    private static final double MODERATE_MIN_KM_H = 25.0;
    private static final double STRONG_MIN_KM_H = 40.0;
    private static final double SEVERE_MIN_KM_H = 55.0;

    @Override
    public NortadaStatus detect(WeatherReading reading) {
        if (reading == null) {
            throw new IllegalArgumentException("Weather reading cannot be null!");
        }

        double degrees = reading.getWindDirection().getDegrees();
        if (!isInSector(degrees)) {
            return NortadaStatus.NONE;
        }

        return gradeBySpeed(reading.getWindSpeed().getKmPerHour());
    }

    private boolean isInSector(double degrees) {
        return degrees >= SECTOR_LOWER_DEGREES || degrees <= SECTOR_UPPER_DEGREES;
    }

    private NortadaStatus gradeBySpeed(double kmPerHour) {
        if (kmPerHour < LIGHT_MIN_KM_H) {
            return NortadaStatus.NONE;
        }
        if (kmPerHour < MODERATE_MIN_KM_H) {
            return NortadaStatus.LIGHT;
        }
        if (kmPerHour < STRONG_MIN_KM_H) {
            return NortadaStatus.MODERATE;
        }
        if (kmPerHour < SEVERE_MIN_KM_H) {
            return NortadaStatus.STRONG;
        }
        return NortadaStatus.SEVERE;
    }
}
