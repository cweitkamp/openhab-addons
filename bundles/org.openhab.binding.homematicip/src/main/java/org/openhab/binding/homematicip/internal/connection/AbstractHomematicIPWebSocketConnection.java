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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.CloseException;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.homematicip.internal.dto.events.Event;
import org.openhab.binding.homematicip.internal.dto.events.Origin;
import org.openhab.binding.homematicip.internal.dto.events.WebSocketEvent;
import org.openhab.core.i18n.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link AbstractHomematicIPWebSocketConnection} is responsible for handling the events send by the homematic IP
 * websocket.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractHomematicIPWebSocketConnection {

    private static final String FIRST = "0";
    private static final String REASON_BROKEN_PIPE = "Broken pipe";
    private static final String REASON_DISCONNECTED = "Disconnected";

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Map<String, HomematicIPWebSocketListener> listeners = new ConcurrentHashMap<>();

    protected final WebSocketClient webSocketClient;
    protected final Gson gson;

    protected final String webSocketName;

    private @Nullable RemoteEndpoint remoteEndpoint;

    private boolean isRunning;

    public AbstractHomematicIPWebSocketConnection(WebSocketClient webSocketClient, Gson gson) {
        this.webSocketClient = webSocketClient;
        this.webSocketClient.setMaxIdleTimeout(0);
        this.webSocketClient.getPolicy().setMaxTextMessageSize(512 * 1024);
        this.gson = gson;

        this.webSocketName = "WebSocket$" + System.currentTimeMillis() + "-" + INSTANCE_COUNTER.incrementAndGet();
    }

    /**
     *
     * @param uri
     * @throws ConnectionException
     */
    abstract void connect(URI uri) throws ConnectionException;

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
        this.remoteEndpoint = session.getRemote();
        isRunning = true;
        handleConnect();

        listeners.forEach((id, listener) -> {
            listener.onConnect();
        });
    }

    abstract void handleConnect();

    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.trace("{} received message: {}", webSocketName, message);

        handleMessage(message);
    }

    abstract void handleMessage(String message);

    public void handleWebsocketEvent(@Nullable WebSocketEvent webSocketEvent) {
        if (webSocketEvent != null) {
            switch (webSocketEvent.origin.originType) {
                case Origin.ORIGIN_TYPE_CLIENT:
                    // TODO do we need a handling for these events?
                    break;
                case Origin.ORIGIN_TYPE_DEVICE:
                    logger.debug("{} received event(s) for device '{}'", webSocketName, webSocketEvent.origin.id);
                    // Map<String, JsonObject> events = webSocketEvent.events.entrySet().stream()
                    // .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsJsonObject()));
                    // Event event = gson.fromJson(events.get(FIRST), Event.class);
                    webSocketEvent.events.entrySet().stream().forEach(e -> {
                        Event event = gson.fromJson(e.getValue(), Event.class);
                        if (event != null) {
                            JsonObject data = null;
                            switch (event.eventType) {
                                case Event.EVENT_TYPE_DEVICE_ADDED:
                                    // TODO
                                    logger.warn("{} received event of unknown type: {}", webSocketName,
                                            event.eventType);
                                    break;
                                case Event.EVENT_TYPE_DEVICE_CHANGED:
                                    data = event.device;
                                    break;
                                case Event.EVENT_TYPE_GROUP_CHANGED:
                                    // TODO handle groups of other types
                                    String type = event.group.get(PROPERTY_TYPE).getAsString();
                                    if (HEATING_GROUP.equals(type)) {
                                        data = event.group;
                                    }
                                    break;
                                case Event.EVENT_TYPE_HOME_CHANGED:
                                    data = event.home;
                                    break;
                                case Event.EVENT_TYPE_INCLUSION_REQUESTED:
                                case Event.EVENT_TYPE_SECURITY_JOURNAL_CHANGED:
                                    // TODO do we need to handle these???
                                    break;
                                default:
                                    logger.warn("{} received event of unknown type: {}", webSocketName,
                                            event.eventType);
                                    break;
                            }
                            if (data != null) {
                                String listenerId = !Event.EVENT_TYPE_HOME_CHANGED.equals(event.eventType)
                                        && data.has(PROPERTY_ID) ? data.get(PROPERTY_ID).getAsString()
                                                : webSocketEvent.origin.id;
                                HomematicIPWebSocketListener listener = listeners.get(listenerId);
                                if (listener != null) {
                                    listener.messageReceived(data);
                                } else {
                                    logger.warn(
                                            "{} listener with id {} not found for event of type '{}'. Probably no Thing added for it yet.",
                                            webSocketName, listenerId, event.eventType);
                                }
                            }
                        }
                    });
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
            logger.warn("{} webSocketEvent is null", webSocketName);
        }
    }

    @OnWebSocketError
    public void onError(@Nullable Session session, Throwable cause) {
        logger.debug("{} connection errored, closing: {}", webSocketName, cause.getMessage());
        if (session != null) {
            session.close();
        }

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
                if (realCause != null) {
                    logger.error("{} connection errored, closing: {} - {}", webSocketName, realCause.getClass(),
                            realCause.getMessage());
                } else {
                    logger.error("{} connection errored, closing: {}", webSocketName, cause.getMessage());
                }
            }
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        synchronized (this) {
            this.remoteEndpoint = null;
        }

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

    /**
     *
     * @param message
     * @throws IOException
     */
    protected void sendMessage(String message) throws IllegalStateException, IOException {
        RemoteEndpoint localRemoteEndpoint;
        synchronized (this) {
            localRemoteEndpoint = this.remoteEndpoint;
        }
        if (localRemoteEndpoint == null) {
            return;
        }
        logger.trace("{} sending message: {}", webSocketName, message);
        localRemoteEndpoint.sendString(message);
    }
}
