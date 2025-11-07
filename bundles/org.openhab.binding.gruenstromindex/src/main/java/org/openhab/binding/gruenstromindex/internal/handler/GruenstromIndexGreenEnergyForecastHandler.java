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
package org.openhab.binding.gruenstromindex.internal.handler;

import static org.openhab.binding.gruenstromindex.internal.GruenstromIndexBindingConstants.*;

import java.time.Instant;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gruenstromindex.internal.config.GruenstromIndexGreenEnergyForecastConfiguration;
import org.openhab.binding.gruenstromindex.internal.config.GruenstromIndexZipcodeConfigOptionProvider;
import org.openhab.binding.gruenstromindex.internal.connection.GrunstromIndexConnection;
import org.openhab.binding.gruenstromindex.internal.dto.Forecast;
import org.openhab.binding.gruenstromindex.internal.dto.GrunstromIndexGreenEnergyForecastData;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link GruenstromIndexGreenEnergyForecastHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GruenstromIndexGreenEnergyForecastHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GruenstromIndexGreenEnergyForecastHandler.class);

    private static final Collection<Class<? extends ThingHandlerService>> SUPPORTED_THING_ACTIONS = Set
            .of(GruenstromIndexZipcodeConfigOptionProvider.class);

    private @NonNullByDefault({}) GruenstromIndexGreenEnergyForecastConfiguration config;

    private @Nullable String zipcode;
    private @Nullable GrunstromIndexGreenEnergyForecastData forecastEnergyData;

    public GruenstromIndexGreenEnergyForecastHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize GruenstromIndexGreenEnergyForecast handler '{}'.", getThing().getUID());
        config = getConfigAs(GruenstromIndexGreenEnergyForecastConfiguration.class);

        boolean configValid = true;
        zipcode = config.zipcode;
        if (zipcode.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    TEXT_OFFLINE_CONF_ERROR_MISSING_ZIPCODE);
            zipcode = null;
            configValid = false;
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            GrunstromIndexGreenEnergyForecastData localForecastEnergyData = forecastEnergyData;
            if (localForecastEnergyData != null) {
                updateChannel(channelUID, localForecastEnergyData.getEnergyForecastData(Instant.now()));
            } else {
                logger.debug("No data available to handle refresh of channel '{}' of group '{}'.",
                        channelUID.getIdWithoutGroup(), channelUID.getGroupId());
            }
        } else {
            logger.debug("The GruenstromIndex binding is a read-only binding and cannot handle command '{}'.", command);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return SUPPORTED_THING_ACTIONS;
    }

    /**
     * Updates data for this location.
     *
     * @param connection {@link GrunstromIndexConnection} instance
     */
    public void updateData(GrunstromIndexConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
        } catch (ConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getRawMessage());
        }
    }

    private boolean requestData(GrunstromIndexConnection connection)
            throws CommunicationException, ConfigurationException {
        logger.debug("Update forecast data of thing '{}'.", getThing().getUID());
        try {
            forecastEnergyData = connection.getGreenEnergyForcastData(zipcode);
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getMessage(), e);
            return false;
        }
    }

    private void updateChannels() {
        logger.debug("Update channels of thing '{}'.", getThing().getUID());
        GrunstromIndexGreenEnergyForecastData localForecastEnergyData = forecastEnergyData;
        if (localForecastEnergyData != null) {
            try {
                Forecast current = localForecastEnergyData.getEnergyForecastData(Instant.now());
                for (Channel channel : getThing().getChannels()) {
                    ChannelUID channelUID = channel.getUID();
                    if (ChannelKind.STATE.equals(channel.getKind()) && channelUID.isInGroup()
                            && channelUID.getGroupId() != null && isLinked(channelUID)) {
                        updateChannel(channelUID, current);
                    }
                }
            } catch (NoSuchElementException e) {
                logger.debug("No current forecast data available to update channels.");
            }
        } else {
            logger.debug("No forecast data available to update channels.");
        }
    }

    private void updateChannel(ChannelUID channelUID, @Nullable Forecast current) {
        String channelGroupId = channelUID.getGroupId();
        if (channelGroupId == null) {
            logger.debug("Cannot update channel '{}' as it has no GroupId", channelUID);
            return;
        }
        switch (channelGroupId) {
            case CHANNEL_GROUP_ENERGY_FORECAST:
                updateCurrentChannelState(channelUID, current);
                updateChannelTimeSeries(channelUID);
                break;
        }
    }

    private void updateCurrentChannelState(ChannelUID channelUID, @Nullable Forecast current) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (current != null) {
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_FORECASTED_GRUENSTROMINDEX:
                    state = new DecimalType(current.gsi);
                    break;
                case CHANNEL_FORECASTED_CARBONDIOXIDE_EMISSIONS:
                    state = new QuantityType<>(current.co2GStandard, Units.GRAM_PER_KILOWATT_HOUR);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No current data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    private void updateChannelTimeSeries(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        GrunstromIndexGreenEnergyForecastData localForecastEnergyData = forecastEnergyData;
        if (localForecastEnergyData != null) {
            TimeSeries timeSeries = null;
            switch (channelId) {
                case CHANNEL_FORECASTED_GRUENSTROMINDEX:
                    timeSeries = localForecastEnergyData.getGrunstromIndexTimeseries();
                    break;
                case CHANNEL_FORECASTED_CARBONDIOXIDE_EMISSIONS:
                    timeSeries = localForecastEnergyData.getCarbondioxideEmissionsTimeseries();
                    break;
            }
            if (timeSeries != null && timeSeries.size() > 0) {
                logger.debug("Update channel '{}' of group '{}' with new timeseries.", channelId, channelGroupId);
                sendTimeSeries(channelUID, timeSeries);
            } else {
                logger.debug("No forecast data available to update timeseries of channel '{}' of group '{}'.",
                        channelId, channelGroupId);
            }
        } else {
            logger.debug("No forecast data available to update timeseries of channel '{}' of group '{}'.", channelId,
                    channelGroupId);
        }
    }
}
