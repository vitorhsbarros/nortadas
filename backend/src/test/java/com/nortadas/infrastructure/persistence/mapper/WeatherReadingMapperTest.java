package com.nortadas.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.WeatherCode;
import com.nortadas.domain.valueobject.WeatherReadingId;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import com.nortadas.domain.weatherreading.WeatherReading;
import com.nortadas.domain.weatherreading.WeatherReadingFactory;
import com.nortadas.infrastructure.persistence.datamodel.WeatherReadingDataModel;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WeatherReadingMapper}: the domain ⇄ data-model translation
 * must be lossless in both directions and rebuild every value object through its
 * validating constructor.
 */
class WeatherReadingMapperTest {

    private final WeatherReadingMapper mapper = new WeatherReadingMapper();

    private static final WeatherReadingId ID = WeatherReadingId.newId();
    private static final BeachId BEACH_ID = BeachId.newId();
    private static final WindSpeed SPEED = new WindSpeed(32.5);
    private static final WindDirection DIRECTION = new WindDirection(350.0);
    private static final double AIR_TEMPERATURE = 21.5;
    private static final double WATER_TEMPERATURE = 18.0;
    // 61 → RAIN: a real WMO code so the int column round-trips through WeatherCode.
    private static final WeatherCode WEATHER_CODE = new WeatherCode(61);
    private static final Instant FETCHED_AT = Instant.parse("2026-07-20T12:00:00Z");

    @Test
    @DisplayName("toDataModel copies every field from the domain reading")
    void toDataModelCopiesEveryField() {
        WeatherReading reading = WeatherReadingFactory.rehydrate(
                ID, BEACH_ID, SPEED, DIRECTION, AIR_TEMPERATURE, WATER_TEMPERATURE, WEATHER_CODE, FETCHED_AT);

        WeatherReadingDataModel dataModel = mapper.toDataModel(reading);

        assertEquals(ID.getValue(), dataModel.getId());
        assertEquals(BEACH_ID.getValue(), dataModel.getBeachId());
        assertEquals(SPEED.getKmPerHour(), dataModel.getWindSpeed());
        assertEquals(DIRECTION.getDegrees(), dataModel.getWindDirection());
        assertEquals(AIR_TEMPERATURE, dataModel.getTemperatureCelsius());
        assertEquals(WATER_TEMPERATURE, dataModel.getWaterTemperatureCelsius());
        assertEquals(WEATHER_CODE.getValue(), dataModel.getWeatherCode());
        assertEquals(FETCHED_AT, dataModel.getFetchedAt());
    }

    @Test
    @DisplayName("toDomain rebuilds the reading with matching value objects")
    void toDomainRebuildsReading() {
        WeatherReadingDataModel dataModel = new WeatherReadingDataModel(
                ID.getValue(), BEACH_ID.getValue(), 32.5, 350.0, AIR_TEMPERATURE, WATER_TEMPERATURE, 61, FETCHED_AT);

        WeatherReading reading = mapper.toDomain(dataModel);

        assertEquals(ID, reading.getId());
        assertEquals(BEACH_ID, reading.getBeachId());
        assertEquals(SPEED, reading.getWindSpeed());
        assertEquals(DIRECTION, reading.getWindDirection());
        assertEquals(AIR_TEMPERATURE, reading.getTemperatureCelsius());
        assertEquals(WATER_TEMPERATURE, reading.getWaterTemperatureCelsius());
        assertEquals(WEATHER_CODE, reading.getWeatherCode());
        assertEquals(FETCHED_AT, reading.getFetchedAt());
    }

    @Test
    @DisplayName("domain -> data model -> domain preserves the full state (sameAs)")
    void roundTripPreservesState() {
        WeatherReading original = WeatherReadingFactory.rehydrate(
                ID, BEACH_ID, SPEED, DIRECTION, AIR_TEMPERATURE, WATER_TEMPERATURE, WEATHER_CODE, FETCHED_AT);

        WeatherReading roundTripped = mapper.toDomain(mapper.toDataModel(original));

        assertTrue(original.sameAs(roundTripped),
                "round-tripped reading should be attribute-identical to the original");
    }

    @Test
    @DisplayName("data model -> domain -> data model preserves every column value")
    void roundTripFromDataModelPreservesColumns() {
        UUID id = UUID.randomUUID();
        UUID beachId = UUID.randomUUID();
        WeatherReadingDataModel original = new WeatherReadingDataModel(
                id, beachId, 12.0, 0.0, 15.0, 14.0, 45, FETCHED_AT);

        WeatherReadingDataModel roundTripped = mapper.toDataModel(mapper.toDomain(original));

        assertEquals(id, roundTripped.getId());
        assertEquals(beachId, roundTripped.getBeachId());
        assertEquals(12.0, roundTripped.getWindSpeed());
        assertEquals(0.0, roundTripped.getWindDirection());
        assertEquals(15.0, roundTripped.getTemperatureCelsius());
        assertEquals(14.0, roundTripped.getWaterTemperatureCelsius());
        assertEquals(45, roundTripped.getWeatherCode());
        assertEquals(FETCHED_AT, roundTripped.getFetchedAt());
    }
}
