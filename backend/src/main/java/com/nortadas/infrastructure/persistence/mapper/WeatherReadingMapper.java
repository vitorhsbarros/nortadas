package com.nortadas.infrastructure.persistence.mapper;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.WeatherReadingId;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import com.nortadas.domain.weatherreading.WeatherReading;
import com.nortadas.domain.weatherreading.WeatherReadingFactory;
import com.nortadas.infrastructure.persistence.datamodel.WeatherReadingDataModel;
import org.springframework.stereotype.Component;

/**
 * Explicit {@link WeatherReading} ⇄ {@link WeatherReadingDataModel} mapper (Pure
 * Fabrication; docs/architecture.md §3, §6). Rehydrating to the domain rebuilds
 * every value object through its validating constructor via
 * {@link WeatherReadingFactory}, keeping the domain invariants enforced on the
 * read path too.
 */
@Component
public class WeatherReadingMapper {

    public WeatherReading toDomain(WeatherReadingDataModel dataModel) {
        return WeatherReadingFactory.rehydrate(
                new WeatherReadingId(dataModel.getId()),
                new BeachId(dataModel.getBeachId()),
                new WindSpeed(dataModel.getWindSpeed()),
                new WindDirection(dataModel.getWindDirection()),
                dataModel.getTemperatureCelsius(),
                dataModel.getWaterTemperatureCelsius(),
                dataModel.getFetchedAt());
    }

    public WeatherReadingDataModel toDataModel(WeatherReading reading) {
        return new WeatherReadingDataModel(
                reading.getId().getValue(),
                reading.getBeachId().getValue(),
                reading.getWindSpeed().getKmPerHour(),
                reading.getWindDirection().getDegrees(),
                reading.getTemperatureCelsius(),
                reading.getWaterTemperatureCelsius(),
                reading.getFetchedAt());
    }
}
