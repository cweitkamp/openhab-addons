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

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Policy;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GrunstromIndexGreenEnergyForecastData} is the Java class used to map the JSON response to a
 * GruenstromIndex API request.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GrunstromIndexGreenEnergyForecastData {
    @SerializedName("forecast")
    public List<Forecast> forecast = List.of();
    @SerializedName("location")
    public @NonNullByDefault({}) Location location;
    @SerializedName("provisioning")
    public @NonNullByDefault({}) Provisioning provisioning;

    /**
     *
     *
     * @param timestamp
     * @return
     */
    public @Nullable Forecast getEnergyForecastData(Instant timestamp) {
        return forecast.stream().filter(element -> !timestamp.isBefore(Instant.ofEpochMilli(element.timeframe.start))
                && timestamp.isBefore(Instant.ofEpochMilli(element.timeframe.end))).findFirst().get();
    }

    /**
     *
     *
     * @return
     */
    public TimeSeries getGrunstromIndexTimeseries() {
        final TimeSeries ts = new TimeSeries(Policy.REPLACE);
        forecast.stream().forEach(element -> {
            Instant i = Instant.ofEpochMilli(element.timeframe.start);
            State s = new DecimalType(element.gsi);
            ts.add(i, s);
        });
        return ts;
    }

    /**
     *
     *
     * @return
     */
    public TimeSeries getCarbondioxideEmissionsTimeseries() {
        final TimeSeries ts = new TimeSeries(Policy.REPLACE);
        forecast.stream().forEach(element -> {
            Instant i = Instant.ofEpochMilli(element.timeframe.start);
            State s = new QuantityType<>(element.co2GStandard, Units.GRAM_PER_KILOWATT_HOUR);
            ts.add(i, s);
        });
        return ts;
    }
}
