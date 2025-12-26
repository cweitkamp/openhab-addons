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
package org.openhab.binding.homematicip.internal.handler;

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.homematicip.internal.connection.HomematicIPWebSocketListener;
import org.openhab.binding.homematicip.internal.discovery.AbstractHomematicIPDeviceDiscoveryService;
import org.openhab.binding.homematicip.internal.dto.CurrentState;
import org.openhab.binding.homematicip.internal.dto.Home;
import org.openhab.binding.homematicip.internal.dto.SecurityZone;
import org.openhab.binding.homematicip.internal.dto.Weather;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AbstractHomematicIPBridgeHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractHomematicIPBridgeHandler extends BaseBridgeHandler
        implements HomematicIPWebSocketListener {

    public static final long INITIAL_DELAY_IN_SECONDS = 15;
    public static final long RECONNECT_DELAY_IN_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final HttpClient httpClient;
    protected final WebSocketFactory webSocketFactory;

    protected @Nullable AbstractHomematicIPDeviceDiscoveryService discoveryService;

    protected @Nullable ScheduledFuture<?> reconnectionJob;

    protected final Gson gson = new Gson();

    public AbstractHomematicIPBridgeHandler(Bridge bridge, HttpClient httpClient, WebSocketFactory webSocketFactory) {
        super(bridge);
        this.httpClient = httpClient;
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localReconnectionJob = reconnectionJob;
        if (localReconnectionJob != null && !localReconnectionJob.isCancelled()) {
            logger.debug("Stop reconnection job.");
            if (localReconnectionJob.cancel(true)) {
                reconnectionJob = null;
            }
        }
    }

    @Override
    public void onClose(String reString) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, TEXT_OFFLINE_WEBSOCKET_CLOSED);
        scheduleWebsocketReconnection();
    }

    @Override
    public void onError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, TEXT_OFFLINE_WEBSOCKET_ERROR);
        scheduleWebsocketReconnection();
    }

    abstract void scheduleWebsocketReconnection();

    protected void updateData(CurrentState currentState) {
        updateWeatherChannels(currentState.home);
        updateDevices(currentState.devices);
        updateGroups(currentState.groups);
    }

    protected void updateWeatherChannels(Home homeData) {
        getThing().getChannels().stream().filter(c -> ChannelKind.STATE.equals(c.getKind())).map(c -> c.getUID())
                .filter(ChannelUID::isInGroup).filter(uid -> CHANNEL_GROUP_WAETHER.equals(uid.getGroupId()))
                .filter(uid -> isLinked(uid)).forEach(channelUID -> {
                    updateWeatherChannel(channelUID, homeData.weather);
                });
    }

    private void updateWeatherChannel(ChannelUID channelUID, Weather weatherData) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                state = HomematicIPUtils.getQuantityTypeState(weatherData.temperature, SIUnits.CELSIUS);
                break;
            case CHANNEL_CONDITION:
                state = HomematicIPUtils.getStringTypeState(weatherData.weatherCondition);
                break;
            case CHANNEL_MIN_TEMPERATURE:
                state = HomematicIPUtils.getQuantityTypeState(weatherData.minTemperature, SIUnits.CELSIUS);
                break;
            case CHANNEL_MAX_TEMPERATURE:
                state = HomematicIPUtils.getQuantityTypeState(weatherData.maxTemperature, SIUnits.CELSIUS);
                break;
            case CHANNEL_HUMIDITY:
                state = HomematicIPUtils.getQuantityTypeState(weatherData.humidity, Units.PERCENT);
                break;
            case CHANNEL_WIND_SPEED:
                state = HomematicIPUtils.getQuantityTypeState(weatherData.windSpeed, SIUnits.KILOMETRE_PER_HOUR);
                break;
            case CHANNEL_WIND_DIRECTION:
                state = HomematicIPUtils.getQuantityTypeState(weatherData.windDirection, Units.DEGREE_ANGLE);
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }

    protected @Nullable SecurityZone toSecurityZone(JsonObject data) {
        try {
            return gson.fromJson(data, SecurityZone.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    protected void updateSecurityChannels(List<@Nullable SecurityZone> securityZones) {
        getThing().getChannels().stream().filter(c -> ChannelKind.STATE.equals(c.getKind())).map(c -> c.getUID())
                .filter(ChannelUID::isInGroup).filter(uid -> CHANNEL_GROUP_SECURITY.equals(uid.getGroupId()))
                .filter(uid -> isLinked(uid)).forEach(channelUID -> {
                    updateSecurityChannel(channelUID, securityZones);
                });
    }

    private void updateSecurityChannel(ChannelUID channelUID, List<@Nullable SecurityZone> securityZones) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_ALARM_MODE:
                state = HomematicIPUtils.getAlarmMode(securityZones);
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }

    abstract void updateDevices(JsonObject deviceData);

    /**
     *
     *
     * @param groupId
     * @param temperature
     */
    abstract void setSetPointTemperature(String groupId, double temperature)
            throws CommunicationException, ConfigurationException;

    /**
     *
     *
     * @param groupId
     * @param mode
     */
    abstract void setControlMode(String groupId, String mode) throws CommunicationException, ConfigurationException;

    /**
     *
     *
     * @param groupId
     * @param profileIndex
     */
    abstract void setActiveProfile(String groupId, String profileIndex)
            throws CommunicationException, ConfigurationException;

    /**
     *
     *
     * @param deviceId
     * @param displayMode
     */
    abstract void setClimateControlDisplayMode(String deviceId, String displayMode)
            throws CommunicationException, ConfigurationException;

    /**
     *
     *
     * @param deviceId
     * @param on
     */
    abstract void setSwitchState(String deviceId, boolean on) throws CommunicationException, ConfigurationException;

    protected void updateGroups(JsonObject groupData) {
        Map<String, JsonObject> groups = groupData.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsJsonObject()));
        // update existing Things
        getThing().getThings().stream().map(childThing -> (AbstractHomematicIPDeviceHandler) childThing.getHandler())
                .filter(Objects::nonNull).forEach(deviceHandler -> {
                    JsonObject data = groups.remove(deviceHandler.id);
                    if (data != null) {
                        deviceHandler.updateChannels(data);
                    } else {
                        logger.debug("Unable to update Thing '{}': no data available.", getThing().getUID());
                    }
                });
        // update security Channels
        List<@Nullable SecurityZone> securityGroups = groups.values().stream().map(this::toSecurityZone)
                .filter(Objects::nonNull).filter(s -> SECURITY_ZONE_GROUP.equals(s.type)).collect(Collectors.toList());
        @SuppressWarnings("null")
        List<String> securityGroupIds = securityGroups.stream().map(s -> s.id).collect(Collectors.toList());
        groups.entrySet().removeIf(e -> securityGroupIds.contains(e.getKey()));
        updateSecurityChannels(securityGroups);
        // new Thing discovered
        if (discoveryService != null) {
            groups.forEach(discoveryService::onGroupAdded);
        }
    }

    protected void updateProperties(JsonObject device) {
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_VENDOR, device.get(PROPERTY_OEM).getAsString());
        properties.put(Thing.PROPERTY_MODEL_ID, device.get(PROPERTY_MODEL_TYPE).getAsString());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.get(PROPERTY_FIRMWARE_VERSION).getAsString());
        updateProperties(properties);
    }
}
