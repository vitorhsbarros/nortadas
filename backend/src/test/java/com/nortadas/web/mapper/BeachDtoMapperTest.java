package com.nortadas.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.nortadas.application.usecase.BeachStatusView;
import com.nortadas.domain.beach.Beach;
import com.nortadas.domain.beach.BeachFactory;
import com.nortadas.domain.municipality.Municipality;
import com.nortadas.domain.municipality.MunicipalityFactory;
import com.nortadas.domain.region.Region;
import com.nortadas.domain.region.RegionFactory;
import com.nortadas.domain.valueobject.BeachId;
import com.nortadas.domain.valueobject.Latitude;
import com.nortadas.domain.valueobject.Longitude;
import com.nortadas.domain.valueobject.MunicipalityId;
import com.nortadas.domain.valueobject.Name;
import com.nortadas.domain.valueobject.NortadaStatus;
import com.nortadas.domain.valueobject.WeatherCode;
import com.nortadas.domain.valueobject.WindDirection;
import com.nortadas.domain.valueobject.WindSpeed;
import com.nortadas.domain.weatherreading.WeatherReading;
import com.nortadas.domain.weatherreading.WeatherReadingFactory;
import com.nortadas.web.dto.BeachResponse;
import com.nortadas.web.dto.WeatherReadingResponse;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pure-JUnit unit tests for {@link BeachDtoMapper#toDetail(BeachStatusView)}
 * (no Spring context; docs/architecture.md §9), focused on the {@code reading}
 * block it builds — in particular that {@link WeatherReading#getWaterTemperatureCelsius()}
 * (issue #65) lands on the DTO's {@code waterTemperature} field and not, say, the
 * air {@code temperature} field, given how close the two sit in
 * {@link WeatherReadingResponse}'s constructor argument list.
 */
class BeachDtoMapperTest {

    private final BeachDtoMapper mapper = new BeachDtoMapper();

    private static Beach beach() {
        Region region = RegionFactory.create(new Name("Norte"));
        Municipality municipality =
                MunicipalityFactory.create(MunicipalityId.of("0107"), new Name("Espinho"), region);
        return BeachFactory.create(
                new Name("Praia Central de Espinho"), new Latitude(41.0), new Longitude(-8.6), municipality);
    }

    @Test
    @DisplayName("maps air and water temperature to their own distinct reading fields, not swapped")
    void toDetailMapsAirAndWaterTemperatureToDistinctFields() {
        Beach beach = beach();
        BeachId beachId = beach.getBeachId();
        WeatherReading reading = WeatherReadingFactory.create(
                beachId,
                new WindSpeed(30.0),
                new WindDirection(340.0),
                18.5,
                21.5,
                new WeatherCode(61),
                Instant.parse("2026-07-20T09:00:00Z"));
        BeachStatusView view = new BeachStatusView(beach, Optional.of(reading), NortadaStatus.MODERATE);

        BeachResponse response = mapper.toDetail(view);

        WeatherReadingResponse readingResponse = response.getReading();
        assertThat(readingResponse).isNotNull();
        assertThat(readingResponse.getWindSpeed()).isEqualTo(30.0);
        assertThat(readingResponse.getWindDirection()).isEqualTo(340.0);
        assertThat(readingResponse.getTemperature()).isEqualTo(18.5);
        assertThat(readingResponse.getWaterTemperature()).isEqualTo(21.5);
        assertThat(readingResponse.getWaterTemperature()).isNotEqualTo(readingResponse.getTemperature());
        assertThat(readingResponse.getWeatherCode()).isEqualTo(61);
        assertThat(readingResponse.getFetchedAt()).isEqualTo("2026-07-20T09:00:00Z");
    }

    @Test
    @DisplayName("omits the reading block (and therefore waterTemperature) when there is no stored reading")
    void toDetailOmitsReadingBlockWhenNoReadingPresent() {
        Beach beach = beach();
        BeachStatusView view = new BeachStatusView(beach, Optional.empty(), NortadaStatus.NONE);

        BeachResponse response = mapper.toDetail(view);

        assertThat(response.getReading()).isNull();
    }
}
