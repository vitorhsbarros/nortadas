-- US008 — Beach Data Seeding (issue #13).
-- Seeds the seven Portuguese coastal regions and an initial set of 20 Norte
-- beaches, from Moledo (Caminha) south to Espinho. Kept to portable ANSI/
-- PostgreSQL DDL so it also runs on H2 in PostgreSQL mode (build context test).
--
-- region.id holds the domain RegionId string form: a short, deterministic,
-- name-derived code (RegionId.fromName) — the accent-stripped, uppercased
-- first three letters of the name: Norte->NOR, Centro->CEN, Lisboa->LIS,
-- Alentejo->ALE, Algarve->ALG, Madeira->MAD, Açores->ACO. Regions are a fixed,
-- curated vocabulary, so this natural code (not a UUID) is the identity.
--
-- beach.id holds a canonical UUID (BeachId), fixed here (not DB-generated) so
-- the beach->region foreign keys stay stable and the data is reproducible
-- across environments. Every value is chosen to satisfy the domain invariants
-- it is mapped back through on read (Name character/length rules,
-- Latitude/Longitude bounds).

INSERT INTO region (id, name) VALUES
    ('NOR', 'Norte'),
    ('CEN', 'Centro'),
    ('LIS', 'Lisboa'),
    ('ALE', 'Alentejo'),
    ('ALG', 'Algarve'),
    ('MAD', 'Madeira'),
    ('ACO', 'Açores');

-- All 20 beaches belong to Norte ('NOR'), ordered north to south.
INSERT INTO beach (id, name, latitude, longitude, region_id) VALUES
    ('17658d73-c951-4444-9ec0-9e8357336758', 'Praia de Moledo',                41.8397, -8.8747, 'NOR'),
    ('60f2958f-775a-4e5b-9d3e-70da95731a79', 'Praia de Vila Praia de Âncora',  41.8136, -8.8710, 'NOR'),
    ('8b5f207a-187f-47d8-a2c4-dead60b59577', 'Praia do Cabedelo',              41.6739, -8.8267, 'NOR'),
    ('a5f4c2a2-d0f3-4230-bee7-80fcf5ea1a54', 'Praia Norte',                    41.6969, -8.8535, 'NOR'),
    ('d4a7532e-9209-4d46-bcfa-9f8ca6cdb7b5', 'Praia de Afife',                 41.7514, -8.8770, 'NOR'),
    ('df8e4fd7-a2d9-4f34-9960-eec4246e2b7b', 'Praia de Ofir',                  41.5153, -8.7906, 'NOR'),
    ('eb5cda42-7194-4c48-8969-7e22eb41a775', 'Praia de Apúlia',                41.4794, -8.7736, 'NOR'),
    ('6538efe4-4c12-4107-b414-aa7d548ad84e', 'Praia da Aguçadoura',            41.4489, -8.7828, 'NOR'),
    ('f6655f49-96c5-4381-a834-0267a79206e6', 'Praia da Póvoa de Varzim',       41.3831, -8.7686, 'NOR'),
    ('ec6c3619-2485-4a99-8c7b-2c9dea0e147f', 'Praia da Árvore',                41.3567, -8.7472, 'NOR'),
    ('be93d6a8-817a-4f7f-b280-56a03d6ce777', 'Praia da Azurara',               41.3406, -8.7436, 'NOR'),
    ('7eafb65b-7fac-4103-b65c-9a1ac6d2a82e', 'Praia de Angeiras',              41.2481, -8.7186, 'NOR'),
    ('abe208f8-0f8d-4543-aaa8-e839dc05e0be', 'Praia de Leça da Palmeira',      41.1939, -8.7075, 'NOR'),
    ('b4ffa9bc-1dbc-4b3f-aa41-cbe5de225775', 'Praia de Matosinhos',            41.1817, -8.7008, 'NOR'),
    ('4419dc72-0b03-475c-9351-d7c61f3bf57d', 'Praia dos Ingleses',             41.1508, -8.6772, 'NOR'),
    ('762b9fa4-9c2b-486f-9b1c-dcd29b1801e6', 'Praia do Homem do Leme',         41.1617, -8.6858, 'NOR'),
    ('9fd4a089-1491-4664-848b-534a213c40df', 'Praia de Miramar',               41.0511, -8.6497, 'NOR'),
    ('4ffd6849-5647-43c5-aea6-fa423a88c1f8', 'Praia da Aguda',                 41.0428, -8.6519, 'NOR'),
    ('8bb56951-271d-4f51-bc55-c062acd7dc56', 'Praia de São Félix da Marinha',  41.0242, -8.6511, 'NOR'),
    ('ae617359-5f5a-4f01-8952-52c51bb5e742', 'Praia Central de Espinho',       41.0083, -8.6428, 'NOR');
