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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.homematicip.internal.config.HomematicIPHomeControlUnitConfiguration;
import org.openhab.binding.homematicip.internal.connection.HomematicIPLocalHTTPConnection;
import org.openhab.binding.homematicip.internal.connection.HomematicIPLocalWebSocketConnection;
import org.openhab.binding.homematicip.internal.connection.HomematicIPWebSocketListener;
import org.openhab.binding.homematicip.internal.discovery.HomematicIPHomeControlUnitDeviceDiscoveryService;
import org.openhab.binding.homematicip.internal.dto.CurrentState;
import org.openhab.binding.homematicip.internal.dto.Home;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPHomeControlUnitHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPHomeControlUnitHandler extends AbstractHomematicIPBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HomematicIPHomeControlUnitHandler.class);

    private HomematicIPHomeControlUnitConfiguration config = new HomematicIPHomeControlUnitConfiguration();
    private @Nullable HomematicIPLocalHTTPConnection httpConnection;
    private @Nullable HomematicIPLocalWebSocketConnection webSocketConnection;

    public HomematicIPHomeControlUnitHandler(Bridge bridge, HttpClient httpClient, WebSocketFactory webSocketFactory) {
        super(bridge, httpClient, webSocketFactory);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(HomematicIPHomeControlUnitConfiguration.class);

        boolean configValid = true;

        httpConnection = new HomematicIPLocalHTTPConnection(httpClient, gson, config);

        if (config.authtoken == null) {
            try {
                String localAuthtoken = httpConnection.requestAuthToken().authToken;
                if (localAuthtoken == null) {
                    configValid = false;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            TEXT_OFFLINE_CONF_ERROR_MISSING_AUTHTHOKEN);
                } else {
                    config.authtoken = localAuthtoken;
                    httpConnection.confirmAuthToken(localAuthtoken);
                }
            } catch (CommunicationException e) {
                configValid = false;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            } catch (ConfigurationException e) {
                configValid = false;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
            }
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);

            String websocketId = thing.getUID().getAsString().replace(':', '-');
            if (websocketId.length() < 4) {
                websocketId = "openHAB-" + BINDING_ID + "-" + websocketId;
            } else if (websocketId.length() > 20) {
                websocketId = websocketId.substring(websocketId.length() - 20);
            }

            SslContextFactory trustAllSslContextFactory = new SslContextFactory.Client( /* trustall= */ true);

            webSocketConnection = new HomematicIPLocalWebSocketConnection(
                    webSocketFactory.createWebSocketClient(websocketId, trustAllSslContextFactory), gson, config);
            // register this as a listener as we want to get notified on successful connection and incoming messages
            webSocketConnection.registerListener(config.SGTIN, this);
            webSocketConnection.connect(URI.create(config.getWebsocketURL()));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        HomematicIPLocalWebSocketConnection wc = this.webSocketConnection;
        if (wc != null) {
            // unregister this as a listener as we want to prevent a reconnection of the websocket during shut down
            wc.unregisterListener(config.SGTIN);
            wc.disconnectAndDestroy();
            wc = null;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(HomematicIPHomeControlUnitDeviceDiscoveryService.class);
    }

    public void registerDiscoveryService(HomematicIPHomeControlUnitDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public void unregisterDiscoveryListener() {
        if (discoveryService != null) {
            discoveryService = null;
        }
    }

    @Override
    public void onConnect() {
        // register listener for existing and initialized Things
        getThing().getThings().stream().map(childThing -> (AbstractHomematicIPDeviceHandler) childThing.getHandler())
                .filter(Objects::nonNull)
                /* .filter(ThingHandlerHelper::isHandlerInitialized) */.forEach(deviceHandler -> {
                    String deviceId = deviceHandler.id;
                    if (webSocketConnection != null && deviceId != null) {
                        webSocketConnection.registerListener(deviceId, deviceHandler);
                    }
                });

        refreshData();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    void scheduleWebsocketReconnection() {
        ScheduledFuture<?> localReconnectionJob = reconnectionJob;
        if (localReconnectionJob == null || localReconnectionJob.isCancelled() || localReconnectionJob.isDone()) {
            logger.debug("Start reconnection job in {} s.", RECONNECT_DELAY_IN_SECONDS);
            reconnectionJob = scheduler.schedule(() -> {
                HomematicIPLocalWebSocketConnection wc = this.webSocketConnection;
                if (wc != null) {
                    wc.disconnect();
                    try {
                        wc.connect(URI.create(config.getWebsocketURL()));
                    } catch (ConnectionException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
                        // retry reconnection
                        scheduler.schedule(this::scheduleWebsocketReconnection, RECONNECT_DELAY_IN_SECONDS,
                                TimeUnit.SECONDS);
                    }
                }
            }, RECONNECT_DELAY_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    public void messageReceived(JsonObject data) {
        if (data.has(PROPERTY_HOME)) {
            if (data.has(PROPERTY_DEVICES) && data.has(PROPERTY_GROUPS)) {
                logger.trace("Update data of Thing '{}'.", getThing().getUID());
                CurrentState currentState = gson.fromJson(data, CurrentState.class);
                if (currentState != null) {
                    updateData(currentState);
                }
            } else if (data.has(PROPERTY_WEATHER)) {
                logger.trace("Update weather data of Thing '{}'.", getThing().getUID());
                Home homeData = gson.fromJson(data, Home.class);
                if (homeData != null) {
                    updateWeatherChannels(homeData);
                }
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        String deviceId = ((AbstractHomematicIPDeviceHandler) childHandler).id;
        if (webSocketConnection != null && deviceId != null) {
            webSocketConnection.registerListener(deviceId, (HomematicIPWebSocketListener) childHandler);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        String deviceId = ((AbstractHomematicIPDeviceHandler) childHandler).id;
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
            }
        }
    }

    @Override
    public void setSetPointTemperature(String groupId, double temperature) throws CommunicationException {
        if (webSocketConnection != null) {
            webSocketConnection.setSetPointTemperature(groupId, temperature);
        }
    }

    @Override
    public void setControlMode(String groupId, String mode) throws CommunicationException {
        if (webSocketConnection != null) {
            webSocketConnection.setControlMode(groupId, mode);
        }
    }

    @Override
    public void setActiveProfile(String groupId, String profileIndex) throws CommunicationException {
        if (webSocketConnection != null) {
            webSocketConnection.setActiveProfile(groupId, profileIndex);
        }
    }

    @Override
    public void setClimateControlDisplayMode(String deviceId, String displayMode) throws CommunicationException {
        if (webSocketConnection != null) {
            webSocketConnection.setClimateControlDisplayMode(deviceId, displayMode);
        }
    }

    @Override
    public void setSwitchState(String deviceId, boolean on) throws CommunicationException {
        if (webSocketConnection != null) {
            webSocketConnection.setSwitchState(deviceId, on);
        }
    }

    private void setSetZonesActivation(boolean external, boolean internal) throws CommunicationException {
        if (webSocketConnection != null) {
            webSocketConnection.setSetZonesActivation(external, internal);
        }
    }

    public void refreshData() {
        if (webSocketConnection != null) {
            webSocketConnection.getSystemState();
        }
    }

    @Override
    public void updateDevices(JsonObject deviceData) {
        Map<String, JsonObject> devices = deviceData.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsJsonObject()));
        // update self
        JsonObject self = devices.remove(config.SGTIN);
        if (self != null) {
            updateProperties(self);
        } else {
            logger.debug("Unable to update properties '{}': no data available.", getThing().getUID());
        }
        // update existing Things
        getThing().getThings().stream().map(childThing -> (AbstractHomematicIPDeviceHandler) childThing.getHandler())
                .filter(Objects::nonNull).forEach(deviceHandler -> {
                    JsonObject data = devices.remove(deviceHandler.id);
                    if (data != null) {
                        deviceHandler.updateProperties(data);
                        deviceHandler.updateChannels(data);
                    } else {
                        logger.debug("Unable to update Thing '{}': no data available.", getThing().getUID());
                    }
                });
        // new Thing discovered
        if (discoveryService != null) {
            devices.forEach(discoveryService::onDeviceAdded);
        }
    }
}
