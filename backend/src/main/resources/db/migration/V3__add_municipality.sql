-- Adds the Municipality aggregate between Region and Beach (issue #43).
-- Filtering beaches by only the 7 NUTS-II regions won't scale once the
-- catalogue grows into the hundreds; municipalities give a finer-grained
-- grouping. Kept to portable ANSI/PostgreSQL DDL so it also runs on H2 in
-- PostgreSQL mode (build context test), like V1/V2.
--
-- municipality.id holds the domain MunicipalityId string form: Portugal's real
-- official INE/DICOFRE municipality code, exactly 4 digits (e.g. '0107' for
-- Espinho). No uniqueness constraint on name: municipality names are already
-- unique by INE code, and a couple of Portuguese municipality names
-- historically repeat across regions, so a name-uniqueness constraint isn't
-- warranted.

CREATE TABLE municipality (
    id        VARCHAR(4) NOT NULL,
    name      VARCHAR(80) NOT NULL,
    region_id VARCHAR(3) NOT NULL,
    CONSTRAINT pk_municipality PRIMARY KEY (id),
    CONSTRAINT fk_municipality_region FOREIGN KEY (region_id) REFERENCES region (id)
);

-- All 9 municipalities seeded here belong to region 'NOR' (Norte).
INSERT INTO municipality (id, name, region_id) VALUES
    ('1602', 'Caminha',              'NOR'),
    ('1609', 'Viana do Castelo',     'NOR'),
    ('0306', 'Esposende',            'NOR'),
    ('1313', 'Póvoa de Varzim',      'NOR'),
    ('1316', 'Vila do Conde',        'NOR'),
    ('1308', 'Matosinhos',           'NOR'),
    ('1312', 'Porto',                'NOR'),
    ('1317', 'Vila Nova de Gaia',    'NOR'),
    ('0107', 'Espinho',              'NOR');

-- Link every already-seeded beach (V2) to its municipality.
ALTER TABLE beach ADD COLUMN municipality_id VARCHAR(4);

UPDATE beach SET municipality_id = '1602'
    WHERE name IN ('Praia de Moledo', 'Praia de Vila Praia de Âncora');

UPDATE beach SET municipality_id = '1609'
    WHERE name IN ('Praia do Cabedelo', 'Praia Norte', 'Praia de Afife');

UPDATE beach SET municipality_id = '0306'
    WHERE name IN ('Praia de Ofir', 'Praia de Apúlia');

UPDATE beach SET municipality_id = '1313'
    WHERE name IN ('Praia da Aguçadoura', 'Praia da Póvoa de Varzim');

UPDATE beach SET municipality_id = '1316'
    WHERE name IN ('Praia da Árvore', 'Praia da Azurara');

UPDATE beach SET municipality_id = '1308'
    WHERE name IN ('Praia de Angeiras', 'Praia de Leça da Palmeira', 'Praia de Matosinhos');

UPDATE beach SET municipality_id = '1312'
    WHERE name IN ('Praia dos Ingleses', 'Praia do Homem do Leme');

UPDATE beach SET municipality_id = '1317'
    WHERE name IN ('Praia de Miramar', 'Praia da Aguda', 'Praia de São Félix da Marinha');

UPDATE beach SET municipality_id = '0107'
    WHERE name IN ('Praia Central de Espinho');

-- Every seeded beach now has a municipality; enforce it and add the FK.
ALTER TABLE beach ALTER COLUMN municipality_id SET NOT NULL;
ALTER TABLE beach ADD CONSTRAINT fk_beach_municipality FOREIGN KEY (municipality_id) REFERENCES municipality (id);
ALTER TABLE beach ADD CONSTRAINT uq_beach_name_municipality UNIQUE (name, municipality_id);

CREATE INDEX idx_beach_municipality_id ON beach (municipality_id);

-- region_id on beach is superseded: region is now reached via municipality.
-- Dependent objects (unique constraint, FK, index) are dropped explicitly
-- first, rather than relying on CASCADE, to stay portable across Postgres and
-- H2's PostgreSQL-compatibility mode. The FK constraint is dropped before the
-- index: H2 treats idx_beach_region_id as backing fk_beach_region, and refuses
-- to drop the index directly while the constraint still depends on it.
ALTER TABLE beach DROP CONSTRAINT uq_beach_name_region;
ALTER TABLE beach DROP CONSTRAINT fk_beach_region;
DROP INDEX idx_beach_region_id;
ALTER TABLE beach DROP COLUMN region_id;
