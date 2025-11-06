/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.gruenstromindex.internal.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.types.TimeSeries;

/**
 * Tests for {@link GrunstromIndexGreenEnergyForecastData} methods.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GrunstromIndexGreenEnergyForecastDataTest extends AbstractJSONTest {

    private static final String PREDICTION_JSON = "prediction.json";

    @Test
    public void testLocationOfEnergyForecastData() throws IOException {
        GrunstromIndexGreenEnergyForecastData energyForecastData = getObjectFromJson(PREDICTION_JSON,
                GrunstromIndexGreenEnergyForecastData.class, gson);
        assertNotNull(energyForecastData);

        assertNotNull(energyForecastData.location);
        assertEquals("32584", energyForecastData.location.zipcode);
        assertEquals("Löhne", energyForecastData.location.place);
    }

    @Test
    public void testProvisioningOfEnergyForecastData() throws IOException {
        GrunstromIndexGreenEnergyForecastData energyForecastData = getObjectFromJson(PREDICTION_JSON,
                GrunstromIndexGreenEnergyForecastData.class, gson);
        assertNotNull(energyForecastData);

        assertNotNull(energyForecastData.provisioning);
    }

    @Test
    public void testFirstElementOfEnergyForcastData() throws IOException {
        GrunstromIndexGreenEnergyForecastData energyForecastData = getObjectFromJson(PREDICTION_JSON,
                GrunstromIndexGreenEnergyForecastData.class, gson);
        assertNotNull(energyForecastData);

        // test first element of the forecast data
        Forecast element = energyForecastData.forecast.get(0);
        assertNotNull(element);

        assertEquals(1762419600, element.epochtime);
        assertEquals(34.199999999999996, element.gsi);
        assertEquals(220, element.co2GStandard);
        assertNotNull(element.timeframe);
        assertEquals(1762419600000L, element.timeframe.start);
        assertEquals(1762423200000L, element.timeframe.end);
        assertEquals("32584", element.zip);
    }

    @Test
    public void testSpecificElementOfEnergyForcastData() throws IOException {
        GrunstromIndexGreenEnergyForecastData energyForecastData = getObjectFromJson(PREDICTION_JSON,
                GrunstromIndexGreenEnergyForecastData.class, gson);
        assertNotNull(energyForecastData);

        // test specific element
        Instant now = ZonedDateTime.of(2025, 11, 6, 15, 30, 0, 0, ZoneId.systemDefault()).toInstant();
        Forecast element = energyForecastData.getEnergyForecastData(now);
        assertNotNull(element);

        assertEquals(1762437600, element.epochtime);
        assertEquals(76.95, element.gsi);
        assertEquals(96, element.co2GStandard);
        assertNotNull(element.timeframe);
        assertEquals(1762437600000L, element.timeframe.start);
        assertEquals(1762441200000L, element.timeframe.end);
        assertEquals("32584", element.zip);
    }

    @Test
    public void testEnergyForcastDataTimeSeries() throws IOException {
        GrunstromIndexGreenEnergyForecastData energyForecastData = getObjectFromJson(PREDICTION_JSON,
                GrunstromIndexGreenEnergyForecastData.class, gson);
        assertNotNull(energyForecastData);

        assertNotNull(energyForecastData.forecast);
        assertThat(energyForecastData.forecast, hasSize(110));

        TimeSeries grunstromIndexTimeseries = energyForecastData.getGrunstromIndexTimeseries();
        assertEquals(energyForecastData.forecast.size(), grunstromIndexTimeseries.size());
        assertEquals(1762419600, grunstromIndexTimeseries.getBegin().getEpochSecond());
        assertEquals(1762812000, grunstromIndexTimeseries.getEnd().getEpochSecond());

        TimeSeries carbondioxideEmissionsTimeseries = energyForecastData.getCarbondioxideEmissionsTimeseries();
        assertEquals(energyForecastData.forecast.size(), carbondioxideEmissionsTimeseries.size());
        assertEquals(1762419600, carbondioxideEmissionsTimeseries.getBegin().getEpochSecond());
        assertEquals(1762812000, carbondioxideEmissionsTimeseries.getEnd().getEpochSecond());
    }
}
