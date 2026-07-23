-- Adds the raw WMO weather-interpretation code (Open-Meteo weather_code, a ww
-- code in [0, 99]) to each weather reading (issue #57). Stored as the source of
-- truth; the coarse, client-facing WeatherCondition category is derived from it
-- in the domain rather than persisted. Kept to portable ANSI/PostgreSQL DDL that
-- also runs on H2 in PostgreSQL mode (build context test), like V1-V5.
--
-- weather_reading is created empty in V4 with no seed rows, so ADD COLUMN ...
-- NOT NULL works without a default. The app is not live: a dev DB that already
-- holds pre-existing readings (from before this migration) should be reset,
-- since those rows have no weather_code to backfill.

ALTER TABLE weather_reading ADD COLUMN weather_code INTEGER NOT NULL;
ALTER TABLE weather_reading ADD CONSTRAINT ck_weather_reading_weather_code
    CHECK (weather_code >= 0 AND weather_code <= 99);
