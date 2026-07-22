-- US008 — Beach Data Seeding (issue #13): distribute beaches across the main
-- coastal regions (Norte, Centro, Lisboa, Alentejo, Algarve). V2 seeded all
-- seven region rows already (region_id values used below already exist),
-- but only Norte had any municipalities/beaches. This migration adds
-- municipalities and beaches for Centro (CEN), Lisboa (LIS), Alentejo (ALE)
-- and Algarve (ALG), five beaches per region, mirroring the scale and style
-- of V2/V3's Norte data. Kept to portable ANSI/PostgreSQL DDL so it also
-- runs on H2 in PostgreSQL mode (build context test).
--
-- municipality.id values are best-effort real INE/DICOFRE codes (demo/seed
-- data, not an authoritative registry) — internally consistent, unique, and
-- 4 digits, as required by MunicipalityId.
--
-- beach.id values are fresh, fixed UUIDv4 literals (not DB-generated), and
-- beach.municipality_id is set directly on INSERT since the column has been
-- NOT NULL since V3 — no retrofit dance needed for brand-new rows.

INSERT INTO municipality (id, name, region_id) VALUES
    ('0109', 'Ílhavo',            'CEN'),
    ('0602', 'Figueira da Foz',   'CEN'),
    ('1008', 'Nazaré',            'CEN'),
    ('1009', 'Marinha Grande',    'CEN'),
    ('1103', 'Cascais',           'LIS'),
    ('1501', 'Almada',            'LIS'),
    ('1111', 'Sintra',            'LIS'),
    ('1108', 'Oeiras',            'LIS'),
    ('1503', 'Grândola',          'ALE'),
    ('1504', 'Odemira',           'ALE'),
    ('1505', 'Sines',             'ALE'),
    ('1506', 'Santiago do Cacém', 'ALE'),
    ('0805', 'Portimão',          'ALG'),
    ('0801', 'Faro',              'ALG'),
    ('0802', 'Lagoa',             'ALG'),
    ('0803', 'Lagos',             'ALG'),
    ('0804', 'Loulé',             'ALG');

-- Centro: 5 beaches across 4 municipalities.
INSERT INTO beach (id, name, latitude, longitude, municipality_id) VALUES
    ('ee31baa9-44e1-4439-b37d-fff038940e11', 'Praia da Costa Nova',        40.6350, -8.7458, '0109'),
    ('9485c541-1f7d-4848-923b-36f8ecd48ac4', 'Praia da Barra',             40.6461, -8.7458, '0109'),
    ('562610b3-d626-42bc-94b6-d12ea6dde818', 'Praia da Figueira da Foz',   40.1500, -8.8700, '0602'),
    ('0658ac83-aeb9-4738-ad9b-111de4e496db', 'Praia da Nazaré',            39.6033, -9.0714, '1008'),
    ('01b07fcb-7cbb-45f9-942b-4f69b9de6966', 'Praia de São Pedro de Moel', 39.7561, -9.0186, '1009');

-- Lisboa: 5 beaches across 4 municipalities.
INSERT INTO beach (id, name, latitude, longitude, municipality_id) VALUES
    ('9a8fd399-eb28-4b8b-8ccf-6315a63bacf3', 'Praia de Carcavelos',        38.6767, -9.3339, '1103'),
    ('bdd83b94-ba58-4d91-a230-388cbf689472', 'Praia do Guincho',           38.7325, -9.4715, '1103'),
    ('1c9b03c9-c83d-4983-b7c4-c03b734fefd5', 'Praia da Costa da Caparica', 38.6425, -9.2371, '1501'),
    ('0c0d9101-c39e-49e5-b992-1e6f1c2d4d41', 'Praia das Maçãs',            38.7897, -9.4728, '1111'),
    ('21289fd0-7b7d-4774-a882-8dfdffdcf0b1', 'Praia de Oeiras',            38.6791, -9.3131, '1108');

-- Alentejo: 5 beaches across 4 municipalities.
INSERT INTO beach (id, name, latitude, longitude, municipality_id) VALUES
    ('aa66ab5a-ff3f-4765-998e-e4b40cd149f0', 'Praia da Comporta',              38.3833, -8.7833, '1503'),
    ('cd22031e-7b2f-416d-9a72-379589d433f3', 'Praia de Vila Nova de Milfontes', 37.7256, -8.7828, '1504'),
    ('449089a4-5f2e-4f4f-bd13-031ade81c431', 'Praia da Zambujeira do Mar',      37.5228, -8.7867, '1504'),
    ('0785b20b-8326-456a-8fcf-4090ddec9d73', 'Praia de Porto Covo',             37.8500, -8.7900, '1505'),
    ('9a84871d-b6bf-4b02-8e85-8903af4d9211', 'Praia da Costa de Santo André',   38.0500, -8.7936, '1506');

-- Algarve: 5 beaches across 5 municipalities.
INSERT INTO beach (id, name, latitude, longitude, municipality_id) VALUES
    ('10ea4b31-b67c-4c60-9160-c62f77dbea1f', 'Praia da Rocha',        37.1194, -8.5383, '0805'),
    ('c72f0818-6c29-4656-a76b-bd6e643ba22a', 'Praia de Faro',         37.0086, -7.9803, '0801'),
    ('7f25dc83-ae06-486e-862c-cb04e358351f', 'Praia da Marinha',      37.0925, -8.4133, '0802'),
    ('e8777339-c07e-4131-9857-cce9fcc7ceca', 'Praia do Camilo',       37.0847, -8.6689, '0803'),
    ('b7b98966-74ee-4723-910b-602dc77bd68f', 'Praia de Vale do Lobo', 37.0611, -8.0011, '0804');
