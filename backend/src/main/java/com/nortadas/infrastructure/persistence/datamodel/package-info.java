/**
 * JPA data models ({@code @Entity} classes: BeachDataModel,
 * MunicipalityDataModel, RegionDataModel, WeatherReadingDataModel) shaped for
 * Hibernate. ORM annotations are confined to this package — the domain layer
 * never carries them (docs/architecture.md §3, §8). Lombok is welcome here.
 * Not currently empty: BeachDataModel and RegionDataModel arrived with US008,
 * MunicipalityDataModel with the Municipality aggregate (issue #43).
 */
package com.nortadas.infrastructure.persistence.datamodel;
