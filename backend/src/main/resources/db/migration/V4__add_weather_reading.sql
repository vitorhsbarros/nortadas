-- Adds the WeatherReading aggregate (issue #14, US009): the per-beach weather
-- time series fetched hourly from Open-Meteo. A reading is its own aggregate
-- root with its own identity, referencing its beach by id rather than being
-- embedded. Kept to portable ANSI/PostgreSQL DDL that also runs on H2 in
-- PostgreSQL mode (build context test), like V1/V2/V3.
--
-- weather_reading.id holds the domain WeatherReadingId (a random UUID); beach_id
-- is a UUID matching beach.id (a UUID), so the foreign key is type-compatible on
-- PostgreSQL. No nortada_status column yet — Nortada detection lands in US010.

CREATE TABLE weather_reading (
    id                        UUID                     NOT NULL,
    beach_id                  UUID                     NOT NULL,
    wind_speed                DOUBLE PRECISION         NOT NULL,
    wind_direction            DOUBLE PRECISION         NOT NULL,
    temperature_celsius       DOUBLE PRECISION         NOT NULL,
    water_temperature_celsius DOUBLE PRECISION         NOT NULL,
    fetched_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_weather_reading PRIMARY KEY (id),
    CONSTRAINT fk_weather_reading_beach FOREIGN KEY (beach_id) REFERENCES beach (id),
    CONSTRAINT ck_weather_reading_wind_speed CHECK (wind_speed >= 0),
    CONSTRAINT ck_weather_reading_wind_direction CHECK (wind_direction >= 0 AND wind_direction < 360)
);

-- Supports the "latest reading per beach" lookup (order by fetched_at within a beach).
CREATE INDEX idx_weather_reading_beach_fetched ON weather_reading (beach_id, fetched_at);
