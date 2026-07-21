-- US007 scaffolding baseline: core schema for regions and beaches.
-- Beach seeding (data) is deliberately left to US008; the weather_reading table
-- arrives with US009 when readings start being stored.
-- Kept to portable ANSI/PostgreSQL DDL that also runs on H2 in PostgreSQL mode
-- (used by the build's context-load test).

-- region.id / beach.region_id hold the domain RegionId string form:
-- 1-3 letter name-derived prefix + '-' + canonical UUID (max 3+1+36 = 40 chars).
CREATE TABLE region (
    id   VARCHAR(40) NOT NULL,
    name VARCHAR(80) NOT NULL,
    CONSTRAINT pk_region PRIMARY KEY (id),
    CONSTRAINT uq_region_name UNIQUE (name)
);

CREATE TABLE beach (
    id        UUID             NOT NULL,
    name      VARCHAR(80)      NOT NULL,
    latitude  DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    region_id VARCHAR(40)      NOT NULL,
    CONSTRAINT pk_beach PRIMARY KEY (id),
    CONSTRAINT fk_beach_region FOREIGN KEY (region_id) REFERENCES region (id),
    CONSTRAINT uq_beach_name_region UNIQUE (name, region_id),
    CONSTRAINT ck_beach_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_beach_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE INDEX idx_beach_region_id ON beach (region_id);
