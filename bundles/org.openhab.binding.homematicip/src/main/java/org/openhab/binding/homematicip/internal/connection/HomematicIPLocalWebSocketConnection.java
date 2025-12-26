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
package org.openhab.binding.homematicip.internal.connection;

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.*;
import static org.openhab.binding.homematicip.internal.dto.messages.HmipSystemRequest.*;
import static org.openhab.binding.homematicip.internal.dto.messages.PluginMessage.*;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.homematicip.internal.config.HomematicIPHomeControlUnitConfiguration;
import org.openhab.binding.homematicip.internal.dto.events.WebSocketEvent;
import org.openhab.binding.homematicip.internal.dto.messages.ConfigTemplateResponse;
import org.openhab.binding.homematicip.internal.dto.messages.ConfigUpdateResponse;
import org.openhab.binding.homematicip.internal.dto.messages.DiscoverResponse;
import org.openhab.binding.homematicip.internal.dto.messages.HmipSystemEvent;
import org.openhab.binding.homematicip.internal.dto.messages.HmipSystemRequest;
import org.openhab.binding.homematicip.internal.dto.messages.HmipSystemResponse;
import org.openhab.binding.homematicip.internal.dto.messages.PluginMessage;
import org.openhab.binding.homematicip.internal.dto.messages.PluginStateResponse;
import org.openhab.binding.homematicip.internal.dto.params.SetActiveProfileParams;
import org.openhab.binding.homematicip.internal.dto.params.SetClimateControlDisplayParams;
import org.openhab.binding.homematicip.internal.dto.params.SetControlModeParams;
import org.openhab.binding.homematicip.internal.dto.params.SetSetPointTemperatureParams;
import org.openhab.binding.homematicip.internal.dto.params.SetSetZonesActivationParams;
import org.openhab.binding.homematicip.internal.dto.params.SetSwitchStateParams;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomematicIPLocalWebSocketConnection} is responsible for handling the events send by the homematic IP
 * websocket.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class HomematicIPLocalWebSocketConnection extends AbstractHomematicIPWebSocketConnection {

    private static final String WSS_HEADER_AUTHTOKEN = "authtoken";
    private static final String WSS_HEADER_PLUGIN_ID = "plugin-id";
    private static final String WSS_HEADER_HMIP_SYSTEM_EVENTS = "hmip-system-events";

    private final Logger logger = LoggerFactory.getLogger(HomematicIPLocalWebSocketConnection.class);

    private final HomematicIPHomeControlUnitConfiguration config;

    public HomematicIPLocalWebSocketConnection(WebSocketClient webSocketClient, Gson gson,
            HomematicIPHomeControlUnitConfiguration config) {
        super(webSocketClient, gson);
        this.config = config;
    }

    @Override
    public void connect(URI uri) throws ConnectionException {
        ClientUpgradeRequest customRequest = new ClientUpgradeRequest();
        customRequest.setHeader(WSS_HEADER_AUTHTOKEN, config.authtoken);
        customRequest.setHeader(WSS_HEADER_PLUGIN_ID, PLUGIN_ID);
        customRequest.setHeader(WSS_HEADER_HMIP_SYSTEM_EVENTS, "true");

        try {
            logger.debug("{} starting websocket", webSocketName);
            webSocketClient.start();
            logger.debug("{} connecting to URL = '{}'", webSocketName, uri);
            webSocketClient.connect(this, uri, customRequest).get();
        } catch (SSLHandshakeException e) {
            String message = e.getMessage();
            logger.warn("{} SSLHandshakeException occurred during execution: {}", webSocketName, message, e);
            throw new ConnectionException(message == null ? TEXT_OFFLINE_WEBSOCKET_SSL_ERROR : message, e.getCause());
        } catch (UpgradeException e) {
            String message = e.getMessage();
            throw new ConnectionException(message == null ? TEXT_OFFLINE_WEBSOCKET_UPGRADE_ERROR : message,
                    e.getCause());
        } catch (NoRouteToHostException e) {
            String message = e.getMessage();
            throw new ConnectionException(message == null ? TEXT_OFFLINE_WEBSOCKET_NO_ROUTE_TO_HOST_ERROR : message,
                    e.getCause());
        } catch (UnknownHostException e) {
            String message = e.getMessage();
            throw new ConnectionException(message == null ? TEXT_OFFLINE_WEBSOCKET_UNKNOWN_HOST_ERROR : message,
                    e.getCause());
        } catch (IllegalArgumentException | IOException e) {
        } catch (Exception e) {
            logger.warn("{} encountered an error while connecting: {}", webSocketName, e.getMessage());
        }
    }

    @Override
    public void handleConnect() {
        internalSendMessage(new PluginStateResponse());
    }

    @Override
    public void handleMessage(String message) {
        try {
            PluginMessage pluginMessage = gson.fromJson(message, PluginMessage.class);
            if (pluginMessage != null) {
                switch (pluginMessage.type) {
                    case CONFIG_TEMPLATE_REQUEST_TYPE:
                        internalSendMessage(new ConfigTemplateResponse(pluginMessage.id));
                        break;
                    case CONFIG_UPDATE_REQUEST_TYPE:
                        internalSendMessage(new ConfigUpdateResponse(pluginMessage.id));
                        break;
                    case CREATE_USER_MESSAGE_RESPONSE_TYPE:
                        // do nothing
                        break;
                    case DISCOVER_REQUEST_TYPE:
                        internalSendMessage(new DiscoverResponse(pluginMessage.id));
                        break;
                    case HMIP_SYSTEM_EVENT_TYPE:
                        HmipSystemEvent systemEvent = gson.fromJson(message, HmipSystemEvent.class);
                        if (systemEvent != null) {
                            handleWebsocketEvent(
                                    gson.fromJson(systemEvent.body.eventTransaction, WebSocketEvent.class));
                        }
                        break;
                    case HMIP_SYSTEM_RESPONSE_TYPE:
                        HmipSystemResponse systemResponse = gson.fromJson(message, HmipSystemResponse.class);
                        if (systemResponse != null) {
                            JsonObject data = systemResponse.body.body;
                            if (data != null) {
                                HomematicIPWebSocketListener listener = listeners.get(config.SGTIN);
                                if (listener != null) {
                                    listener.messageReceived(data);
                                } else {
                                    logger.warn(
                                            "{} listener for device '{}' not found. Probably no Thing added for it yet.",
                                            webSocketName, config.SGTIN);
                                }
                            }
                        }
                        break;
                    case PLUGIN_STATE_REQUEST_TYPE:
                        internalSendMessage(new PluginStateResponse(pluginMessage.id));
                        break;
                    default:
                        logger.warn("{} received message of unhandled type: {}", webSocketName, message);
                        break;
                }
            } else {
                logger.warn("{} pluginMessage is null", webSocketName);
            }
        } catch (JsonSyntaxException e) {
            logger.warn("{} JsonSyntaxException: {}", webSocketName, e.getMessage());
        }
    }

    public void getSystemState() throws CommunicationException {
        internalSendMessage(new HmipSystemRequest(HMIP_HOME_GET_SYSTEM_STATE_PATH));
    }

    public void setSetPointTemperature(String groupId, double temperature) throws CommunicationException {
        SetSetPointTemperatureParams setPointTemperatureParams = new SetSetPointTemperatureParams();
        setPointTemperatureParams.groupId = groupId;
        setPointTemperatureParams.setPointTemperature = temperature;
        internalSendMessage(new HmipSystemRequest(HMIP_GROUP_HEATING_SET_SETPOINT_TEMPERATURE_PATH)
                .withBody(setPointTemperatureParams));
    }

    public void setControlMode(String groupId, String mode) throws CommunicationException {
        SetControlModeParams controlModeParams = new SetControlModeParams();
        controlModeParams.groupId = groupId;
        controlModeParams.controlMode = mode;
        internalSendMessage(
                new HmipSystemRequest(HMIP_GROUP_HEATING_SET_CONTROL_MODE_PATH).withBody(controlModeParams));
    }

    public void setActiveProfile(String groupId, String profileIndex) throws CommunicationException {
        SetActiveProfileParams activeProfileParams = new SetActiveProfileParams();
        activeProfileParams.groupId = groupId;
        activeProfileParams.profileIndex = profileIndex;
        internalSendMessage(
                new HmipSystemRequest(HMIP_GROUP_HEATING_SET_ACTIVE_PROFILE_PATH).withBody(activeProfileParams));
    }

    public void setClimateControlDisplayMode(String deviceId, String displayMode) throws CommunicationException {
        SetClimateControlDisplayParams climateControlDisplayParams = new SetClimateControlDisplayParams();
        climateControlDisplayParams.deviceId = deviceId;
        climateControlDisplayParams.display = displayMode;
        // TODO
        // internalSendMessage(new HmipSystemRequest(...) .withBody(climateControlDisplayParams));
    }

    public void setSwitchState(String deviceId, boolean on) throws CommunicationException {
        SetSwitchStateParams switchStateParams = new SetSwitchStateParams();
        switchStateParams.deviceId = deviceId;
        switchStateParams.on = on;
        internalSendMessage(new HmipSystemRequest(HMIP_CONTROL_SET_SWITCH_STATE_PATH).withBody(switchStateParams));
    }

    public void setSetZonesActivation(boolean external, boolean internal) throws CommunicationException {
        SetSetZonesActivationParams setSetZonesActivationParams = new SetSetZonesActivationParams();
        setSetZonesActivationParams.zonesActivation.external = external;
        setSetZonesActivationParams.zonesActivation.internal = internal;
        internalSendMessage(new HmipSystemRequest(HMIP_HOME_SECURITY_SET_ZONES_ACTIVATION_PATH)
                .withBody(setSetZonesActivationParams));
    }

    private void internalSendMessage(PluginMessage pluginMessage) {
        try {
            String message = gson.toJson(pluginMessage);
            sendMessage(message);
        } catch (IllegalStateException e) {
            logger.warn("{} IllegalStateException occurred during execution: {} ", webSocketName, e.getMessage());
        } catch (IOException e) {
            logger.warn("{} IOException occurred during execution: {} ", webSocketName, e.getMessage());
        }
    }
}
