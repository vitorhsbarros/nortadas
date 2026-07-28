package com.nortadas.domain.valueobject;

/**
 * A coarse, client-facing weather category <em>derived</em> from a raw
 * {@link WeatherCode} (WMO {@code ww} code) — the weather counterpart to the way
 * a {@code NortadaStatus} is derived from a stored reading: the
 * {@link com.nortadas.domain.weatherreading.WeatherReading} stores the raw code
 * as source of truth, and this category is computed on demand.
 *
 * <p>The mapping from WMO code to category (see {@link #fromWmoCode(WeatherCode)})
 * is total — every code in {@code [0, 99]} maps somewhere, with codes that don't
 * fall in a recognised group falling back to {@link #UNKNOWN}:
 * <ul>
 *   <li>{@code 0} → {@link #CLEAR}</li>
 *   <li>{@code 1, 2, 3} → {@link #CLOUDY}</li>
 *   <li>{@code 45, 48} → {@link #FOG}</li>
 *   <li>{@code 51, 53, 55, 56, 57} → {@link #DRIZZLE}</li>
 *   <li>{@code 61, 63, 65, 66, 67, 80, 81, 82} → {@link #RAIN} (continuous rain + rain showers)</li>
 *   <li>{@code 71, 73, 75, 77, 85, 86} → {@link #SNOW} (snowfall + snow showers)</li>
 *   <li>{@code 95, 96, 99} → {@link #THUNDERSTORM}</li>
 *   <li>any other value in {@code [0, 99]} → {@link #UNKNOWN}</li>
 * </ul>
 */
public enum WeatherCondition {

    CLEAR,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM,
    UNKNOWN;

    /**
     * Maps a raw WMO {@link WeatherCode} to its coarse category. Total over the
     * valid code range: any code without a recognised grouping returns
     * {@link #UNKNOWN}.
     *
     * @throws IllegalArgumentException if {@code code} is {@code null}
     */
    public static WeatherCondition fromWmoCode(WeatherCode code) {
        if (code == null) {
            throw new IllegalArgumentException("Weather code cannot be null!");
        }

        switch (code.getValue()) {
            case 0:
                return CLEAR;
            case 1:
            case 2:
            case 3:
                return CLOUDY;
            case 45:
            case 48:
                return FOG;
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
                return DRIZZLE;
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
            case 80:
            case 81:
            case 82:
                return RAIN;
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return SNOW;
            case 95:
            case 96:
            case 99:
                return THUNDERSTORM;
            default:
                return UNKNOWN;
        }
    }
}
