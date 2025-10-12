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

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.homematicip.internal.config.HomematicIPAccessPointConfiguration;
import org.openhab.binding.homematicip.internal.connection.HomematicIPHTTPConnection;
import org.openhab.binding.homematicip.internal.connection.HomematicIPWebSocketConnection;
import org.openhab.binding.homematicip.internal.connection.HomematicIPWebSocketListener;
import org.openhab.binding.homematicip.internal.discovery.HomematicIPDeviceDiscoveryService;
import org.openhab.binding.homematicip.internal.dto.CurrentState;
import org.openhab.binding.homematicip.internal.dto.Home;
import org.openhab.binding.homematicip.internal.dto.Host;
import org.openhab.binding.homematicip.internal.dto.SecurityZone;
import org.openhab.binding.homematicip.internal.dto.Weather;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomematicIPAccessPointHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPAccessPointHandler extends BaseBridgeHandler implements HomematicIPWebSocketListener {

    private final Logger logger = LoggerFactory.getLogger(HomematicIPAccessPointHandler.class);

    private static final long INITIAL_DELAY_IN_SECONDS = 10;

    private final HttpClient httpClient;
    private final WebSocketFactory webSocketFactory;

    private HomematicIPAccessPointConfiguration config = new HomematicIPAccessPointConfiguration();
    private @Nullable HomematicIPDeviceDiscoveryService discoveryService;
    private @Nullable HomematicIPHTTPConnection httpConnection;
    private @Nullable HomematicIPWebSocketConnection webSocketConnection;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> reconnectionJob;

    final Gson gson = new Gson();

    public HomematicIPAccessPointHandler(Bridge bridge, HttpClient httpClient, WebSocketFactory webSocketFactory) {
        super(bridge);
        this.httpClient = httpClient;
        this.webSocketFactory = webSocketFactory;
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(HomematicIPAccessPointConfiguration.class);

        boolean configValid = true;
        try {
            config.generateClientauth();
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            logger.error("Could not create SHA-512 hash for client authentication token.", e);
            configValid = false;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);

            String websocketId = thing.getUID().getAsString().replace(':', '-');
            if (websocketId.length() < 4) {
                websocketId = "openHAB-" + BINDING_ID + "-" + websocketId;
            } else if (websocketId.length() > 20) {
                websocketId = websocketId.substring(websocketId.length() - 20);
            }

            httpConnection = new HomematicIPHTTPConnection(httpClient, gson, config);
            Host host = Objects.requireNonNullElse(httpConnection.lookupHost(), new Host());
            httpConnection.setBaseUrl(host.urlREST);

            webSocketConnection = new HomematicIPWebSocketConnection(
                    webSocketFactory.createWebSocketClient(websocketId), gson, config);
            webSocketConnection.connect(URI.create(host.urlWebSocket));
            webSocketConnection.registerListener(config.SGTIN, this);

            ScheduledFuture<?> localRefreshJob = refreshJob;
            if (localRefreshJob == null || localRefreshJob.isCancelled() || localRefreshJob.isDone()) {
                logger.debug("Start refresh job at interval {} min.", config.refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateData, INITIAL_DELAY_IN_SECONDS,
                        TimeUnit.MINUTES.toSeconds(config.refreshInterval), TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
            logger.debug("Stop refresh job.");
            if (localRefreshJob.cancel(true)) {
                refreshJob = null;
            }
        }
        ScheduledFuture<?> localReconnectionJob = reconnectionJob;
        if (localReconnectionJob != null && !localReconnectionJob.isCancelled()) {
            logger.debug("Stop reconnection job.");
            if (localReconnectionJob.cancel(true)) {
                reconnectionJob = null;
            }
        }
        HomematicIPWebSocketConnection wc = this.webSocketConnection;
        if (wc != null) {
            // unregister this as a listener as we want to prevent a reconnection of the websocket during shut down
            wc.unregisterListener(config.SGTIN);
            wc.disconnectAndDestroy();
            wc = null;
        }
    }

    @Override
    public void onConnect() {
        // register listener for existing and initialized Things
        getThing().getThings().stream().map(childThing -> (HomematicIPAbstractDeviceHandler) childThing.getHandler())
                .filter(Objects::nonNull)
                /* .filter(ThingHandlerHelper::isHandlerInitialized) */.forEach(deviceHandler -> {
                    String deviceId = deviceHandler.id;
                    if (webSocketConnection != null && deviceId != null) {
                        webSocketConnection.registerListener(deviceId, deviceHandler);
                    }
                });
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

    private void scheduleWebsocketReconnection() {
        ScheduledFuture<?> localReconnectionJob = reconnectionJob;
        if (localReconnectionJob == null || localReconnectionJob.isCancelled() || localReconnectionJob.isDone()) {
            logger.debug("Start reconnection job in {} s.", INITIAL_DELAY_IN_SECONDS);
            reconnectionJob = scheduler.schedule(() -> {
                HomematicIPWebSocketConnection wc = this.webSocketConnection;
                if (wc != null) {
                    wc.disconnect();

                    HomematicIPHTTPConnection hc = this.httpConnection;
                    if (hc != null) {
                        Host host = Objects.requireNonNullElse(hc.lookupHost(), new Host());
                        hc.setBaseUrl(host.urlREST);

                        try {
                            wc.connect(URI.create(host.urlWebSocket));
                        } catch (ConnectionException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
                            // retry reconnection
                            scheduler.schedule(this::scheduleWebsocketReconnection, INITIAL_DELAY_IN_SECONDS,
                                    TimeUnit.SECONDS);
                        }
                    } else {
                        logger.error(
                                "Unable to reconnect websocket. Restart websocket connection manually e.g. by disabling and reenabling Thing '{}'.",
                                thing.getThingTypeUID());
                    }
                }
            }, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    public void messageReceived(JsonObject data) {
        if (data.has(PROPERTY_WEATHER)) {
            Home homeData = gson.fromJson(data, Home.class);
            if (homeData != null) {
                updateWeatherChannels(homeData);
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        String deviceId = ((HomematicIPAbstractDeviceHandler) childHandler).id;
        if (webSocketConnection != null && deviceId != null) {
            webSocketConnection.registerListener(deviceId, (HomematicIPWebSocketListener) childHandler);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        String deviceId = ((HomematicIPAbstractDeviceHandler) childHandler).id;
        if (webSocketConnection != null && deviceId != null) {
            webSocketConnection.unregisterListener(deviceId);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (command == RefreshType.REFRESH) {
            logger.debug("Cannot handle command '{}' for Channel '{}#{}' of Thing '{}'", command, channelGroupId,
                    channelId, getThing().getUID());
            return;
        }
        switch (channelGroupId + ChannelUID.CHANNEL_GROUP_SEPARATOR + channelId) {
            case CHANNEL_GROUP_SECURITY + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ALARM_MODE:
                handleAlarmModeCommand(command);
                break;
            default:
                logger.debug("Channel '{}#{}' of Thing '{}' is read-only and cannot handle command '{}'.",
                        channelGroupId, channelId, getThing().getUID(), command);
                break;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(HomematicIPDeviceDiscoveryService.class);
    }

    /**
     *
     * @param discoveryService
     */
    public void registerDiscoveryService(HomematicIPDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     *
     */
    public void unregisterDiscoveryListener() {
        if (discoveryService != null) {
            discoveryService = null;
        }
    }

    private void handleAlarmModeCommand(Command command) {
        if (command instanceof StringType) {
            try {
                switch (command.toString()) {
                    case HomematicIPUtils.ALARM_MODE_NONE:
                        setSetZonesActivation(false, false);
                        break;
                    case HomematicIPUtils.ALARM_MODE_EXTERNAL:
                        setSetZonesActivation(true, false);
                        break;
                    case HomematicIPUtils.ALARM_MODE_INTERNAL:
                        setSetZonesActivation(true, true);
                        break;
                }
            } catch (CommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
            } catch (ConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getRawMessage());
            }
        }
    }

    private void setSetZonesActivation(boolean external, boolean internal)
            throws CommunicationException, ConfigurationException {
        if (httpConnection != null) {
            httpConnection.setSetZonesActivation(external, internal);
        }
    }

    /**
     *
     *
     * @param groupId
     * @param temperature
     */
    public void setSetPointTemperature(String groupId, double temperature)
            throws CommunicationException, ConfigurationException {
        if (httpConnection != null) {
            httpConnection.setSetPointTemperature(groupId, temperature);
        }
    }

    /**
     *
     *
     * @param groupId
     * @param mode
     */
    public void setControlMode(String groupId, String mode) throws CommunicationException, ConfigurationException {
        if (httpConnection != null) {
            httpConnection.setControlMode(groupId, mode);
        }
    }

    /**
     *
     *
     * @param groupId
     * @param profileIndex
     */
    public void setActiveProfile(String groupId, String profileIndex)
            throws CommunicationException, ConfigurationException {
        if (httpConnection != null) {
            httpConnection.setActiveProfile(groupId, profileIndex);
        }
    }

    /**
     *
     *
     * @param deviceId
     * @param displayMode
     */
    public void setClimateControlDisplayMode(String deviceId, String displayMode)
            throws CommunicationException, ConfigurationException {
        if (httpConnection != null) {
            httpConnection.setClimateControlDisplayMode(deviceId, displayMode);
        }
    }

    /**
     *
     *
     * @param deviceId
     * @param on
     */
    public void setSwitchState(String deviceId, boolean on) throws CommunicationException, ConfigurationException {
        if (httpConnection != null) {
            httpConnection.setSwitchState(deviceId, on);
        }
    }

    /**
     *
     */
    public void updateData() {
        try {
            if (httpConnection != null) {
                CurrentState currentState = httpConnection.getCurrentState();
                if (currentState != null) {
                    updateWeatherChannels(currentState.home);
                    updateDevices(currentState.devices);
                    updateGroups(currentState.groups);
                }
            }
        } catch (JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
        } catch (ConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getRawMessage());
        }
    }

    private void updateWeatherChannels(Home homeData) {
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

    private @Nullable SecurityZone toSecurityZone(JsonObject data) {
        try {
            return gson.fromJson(data, SecurityZone.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    private void updateSecurityChannels(List<@Nullable SecurityZone> securityZones) {
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

    private void updateDevices(JsonObject deviceData) {
        Map<String, JsonObject> devices = deviceData.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsJsonObject()));
        // update self
        JsonObject self = devices.remove(config.SGTIN);
        if (self != null) {
            updateProperties(self);
        } else {
            logger.debug("Unable to update properties '{}': no data available.", getThing().getThingTypeUID());
        }
        // update existing Things
        getThing().getThings().stream().map(childThing -> (HomematicIPAbstractDeviceHandler) childThing.getHandler())
                .filter(Objects::nonNull).forEach(deviceHandler -> {
                    JsonObject data = devices.remove(deviceHandler.id);
                    if (data != null) {
                        deviceHandler.updateProperties(data);
                        deviceHandler.updateChannels(data);
                    } else {
                        logger.debug("Unable to update Thing '{}': no data available.", thing.getThingTypeUID());
                    }
                });
        // new Thing discovered
        if (discoveryService != null) {
            devices.forEach(discoveryService::onDeviceAdded);
        }
    }

    private void updateGroups(JsonObject groupData) {
        Map<String, JsonObject> groups = groupData.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsJsonObject()));
        // update existing Things
        getThing().getThings().stream().map(childThing -> (HomematicIPAbstractDeviceHandler) childThing.getHandler())
                .filter(Objects::nonNull).forEach(deviceHandler -> {
                    JsonObject data = groups.remove(deviceHandler.id);
                    if (data != null) {
                        deviceHandler.updateChannels(data);
                    } else {
                        logger.debug("Unable to update Thing '{}': no data available.", thing.getThingTypeUID());
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

    private void updateProperties(JsonObject device) {
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_VENDOR, device.get(PROPERTY_OEM).getAsString());
        properties.put(Thing.PROPERTY_MODEL_ID, device.get(PROPERTY_MODEL_TYPE).getAsString());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.get(PROPERTY_FIRMWARE_VERSION).getAsString());
        updateProperties(properties);
    }
}
