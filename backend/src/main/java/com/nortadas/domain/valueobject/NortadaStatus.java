package com.nortadas.domain.valueobject;

/**
 * The graded classification derived from a {@link WeatherReading}, reflecting how
 * strong the Nortada is. Represents the <em>result</em> of the detection rule,
 * not the rule itself — the grading logic (wind-sector gate and km/h thresholds,
 * US010) lives in a dedicated detection service.
 */
public enum NortadaStatus {

    /** Off the N–NNW sector, or sustained speed below the lightest threshold. */
    NONE,

    LIGHT,

    MODERATE,

    STRONG,

    SEVERE
}
