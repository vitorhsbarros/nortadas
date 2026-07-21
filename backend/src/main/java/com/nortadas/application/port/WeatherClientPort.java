package com.nortadas.application.port;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.weatherreading.WeatherReading;

/**
 * Outbound port for fetching current weather from an external provider
 * (docs/architecture.md §1, §5, ADR-004). The application depends on this
 * interface; the Open-Meteo HTTP adapter implements it (DIP / Protected
 * Variations), so the provider's JSON shape never leaks past the
 * {@code infrastructure} boundary. The port speaks the domain: it returns a
 * fully-built {@link WeatherReading}, so the adapter owns the JSON → domain
 * mapping.
 */
public interface WeatherClientPort {

    /**
     * Fetches the current weather for a beach at the given coordinates and
     * returns it as a domain {@link WeatherReading}. Implementations throw an
     * unchecked exception if the fetch fails; callers decide how to isolate that.
     */
    WeatherReading fetchCurrent(BeachId beachId, Latitude latitude, Longitude longitude);
}
