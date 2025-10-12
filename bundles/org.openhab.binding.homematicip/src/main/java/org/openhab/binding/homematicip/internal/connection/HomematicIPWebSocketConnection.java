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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.net.ssl.SSLHandshakeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.CloseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.homematicip.internal.config.HomematicIPAccessPointConfiguration;
import org.openhab.binding.homematicip.internal.dto.events.Event;
import org.openhab.binding.homematicip.internal.dto.events.Origin;
import org.openhab.binding.homematicip.internal.dto.events.WebSocketEvent;
import org.openhab.core.i18n.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomematicIPWebSocketConnection} is responsible for handling the events send by the homematic IP websocket.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class HomematicIPWebSocketConnection {

    private static final String REASON_BROKEN_PIPE = "Broken pipe";
    private static final String REASON_DISCONNECTED = "Disconnected";

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(HomematicIPWebSocketConnection.class);

    private final Map<String, HomematicIPWebSocketListener> listeners = new ConcurrentHashMap<>();

    private final WebSocketClient webSocketClient;
    private final Gson gson;
    private final HomematicIPAccessPointConfiguration config;

    private final String webSocketName;

    private boolean isRunning;

    public HomematicIPWebSocketConnection(WebSocketClient webSocketClient, Gson gson,
            HomematicIPAccessPointConfiguration config) {
        this.webSocketClient = webSocketClient;
        this.gson = gson;
        this.config = config;

        this.webSocketName = "WebSocket$" + System.currentTimeMillis() + "-" + INSTANCE_COUNTER.incrementAndGet();
    }

    public void connect(URI uri) throws ConnectionException {
        ClientUpgradeRequest customRequest = new ClientUpgradeRequest();
        customRequest.setHeader(HTTP_HEADER_AUTHTOKEN, config.authcode);
        customRequest.setHeader(HTTP_HEADER_CLIENTAUTH, config.clientauth);

        try {
            logger.debug("{} starting websocket", webSocketName);
            webSocketClient.start();
            logger.debug("{} connecting to URL = '{}'", webSocketName, uri);
            webSocketClient.connect(this, uri, customRequest).get();
        } catch (SSLHandshakeException e) {
            String message = e.getMessage();
            logger.warn("SSLHandshakeException occurred during execution: {}", message, e);
            throw new ConnectionException(message == null ? TEXT_OFFLINE_WEBSOCKET_SSL_ERROR : message, e.getCause());
        } catch (IllegalArgumentException | IOException e) {
        } catch (Exception e) {
            logger.warn("{} encountered an error while connecting: {}", webSocketName, e.getMessage());
        }
    }

    public void disconnect() {
        logger.debug("{} stopping websocket", webSocketName);
        isRunning = false;

        try {
            webSocketClient.stop();
        } catch (Exception e) {
            logger.warn("{} encountered an error while closing connection: {}", webSocketName, e.getMessage());
        }
    }

    public void disconnectAndDestroy() {
        disconnect();
        webSocketClient.destroy();
    }

    public void registerListener(String deviceId, HomematicIPWebSocketListener listener) {
        logger.trace("{} registering listener for device '{}'", webSocketName, deviceId);
        listeners.put(deviceId, listener);
    }

    public void unregisterListener(String deviceId) {
        listeners.remove(deviceId);
        logger.trace("{} removed listener for device '{}'", webSocketName, deviceId);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.debug("{} connection established to {}", webSocketName, session.getRemoteAddress().getHostString());
        isRunning = true;

        listeners.forEach((id, listener) -> {
            listener.onConnect();
        });
    }

    @OnWebSocketMessage
    public void onMessage(Session session, byte buf[], int offset, int length) {
        String message = new String(buf, StandardCharsets.UTF_8);
        logger.trace("{} received raw data: {}", webSocketName, message);

        try {
            WebSocketEvent webSocketEvent = gson.fromJson(message, WebSocketEvent.class);
            if (webSocketEvent != null) {
                switch (webSocketEvent.origin.originType) {
                    case Origin.ORIGIN_TYPE_CLIENT:
                        // TODO do we need a handling for these events?
                        break;
                    case Origin.ORIGIN_TYPE_DEVICE:
                        logger.debug("{} received event for device '{}'", webSocketName, webSocketEvent.origin.id);
                        Map<String, JsonObject> events = webSocketEvent.events.entrySet().stream()
                                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsJsonObject()));
                        Event event = gson.fromJson(events.get("0"), Event.class);
                        if (event != null) {
                            JsonObject data = Event.EVENT_TYPE_HOME_CHANGED.equals(event.eventType) ? event.home
                                    : event.device;
                            if (data != null) {
                                HomematicIPWebSocketListener listener = listeners.get(webSocketEvent.origin.id);
                                if (listener != null) {
                                    listener.messageReceived(data);
                                } else {
                                    logger.warn(
                                            "{} listener for device '{}' not found. Probably no Thing added a for it yet.",
                                            webSocketName, webSocketEvent.origin.id);
                                }
                            } else {
                                logger.warn("{} device '{}' is null: {}", webSocketName, webSocketEvent.origin.id,
                                        events.get("0"));
                            }
                        } else {
                            logger.warn("{} event is null", webSocketName);
                        }
                        break;
                    case Origin.ORIGIN_TYPE_INTERNAL:
                    case Origin.ORIGIN_TYPE_RULE:
                        // TODO do we need a handling for these events?
                        break;
                    default:
                        logger.warn("{} received event from unknown origin of type: {}", webSocketName,
                                webSocketEvent.origin.originType);
                        break;
                }
            } else {
                logger.warn("{} events is null", webSocketName);
            }
        } catch (JsonSyntaxException e) {
            logger.warn("{} JsonSyntaxException: {}", webSocketName, e.getMessage());
        }
    }

    @OnWebSocketError
    public void onError(@Nullable Session session, Throwable cause) {
        logger.debug("{} connection errored, closing: {}", webSocketName, cause.getMessage());
        if (cause instanceof CloseException) {
            Throwable realCause = cause.getCause();
            if (realCause instanceof TimeoutException) {
                logger.warn("{} connection timed out, reconnecting: {}", webSocketName, realCause.getMessage());
                isRunning = false;

                listeners.forEach((id, listener) -> {
                    listener.onError();
                });
            } else {
                // TODO improve websocket disconnection error
                logger.error("{} connection errored, closing: {} - {}", webSocketName, realCause.getClass(),
                        realCause.getMessage());
            }
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        switch (reason) {
            case REASON_BROKEN_PIPE:
            case REASON_DISCONNECTED:
                logger.debug("{} connection closed: {} / {}", webSocketName, statusCode, reason);
                break;
            default:
                logger.warn("{} connection closed: {} / {}", webSocketName, statusCode, reason);
                break;
        }
        // websocket connection was closed unexpectedly
        if (isRunning) {
            isRunning = false;

            listeners.forEach((id, listener) -> {
                listener.onClose(reason);
            });
        }
    }
}
