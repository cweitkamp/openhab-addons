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

import javax.net.ssl.SSLHandshakeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.homematicip.internal.config.HomematicIPAccessPointConfiguration;
import org.openhab.binding.homematicip.internal.dto.events.WebSocketEvent;
import org.openhab.core.i18n.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomematicIPCloudWebSocketConnection} is responsible for handling the events send by the homematic IP
 * websocket.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class HomematicIPCloudWebSocketConnection extends AbstractHomematicIPWebSocketConnection {

    private final Logger logger = LoggerFactory.getLogger(HomematicIPCloudWebSocketConnection.class);

    private final HomematicIPAccessPointConfiguration config;

    public HomematicIPCloudWebSocketConnection(WebSocketClient webSocketClient, Gson gson,
            HomematicIPAccessPointConfiguration config) {
        super(webSocketClient, gson);
        this.config = config;
    }

    @Override
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
            logger.warn("{} SSLHandshakeException occurred during execution: {}", webSocketName, message, e);
            throw new ConnectionException(message == null ? TEXT_OFFLINE_WEBSOCKET_SSL_ERROR : message, e.getCause());
        } catch (IllegalArgumentException | IOException e) {
        } catch (Exception e) {
            logger.warn("{} encountered an error while connecting: {}", webSocketName, e.getMessage());
        }
    }

    @Override
    public void handleConnect() {
    }

    @Override
    public void handleMessage(String message) {
        try {
            handleWebsocketEvent(gson.fromJson(message, WebSocketEvent.class));
        } catch (JsonSyntaxException e) {
            logger.warn("{} JsonSyntaxException: {}", webSocketName, e.getMessage());
        }
    }
}
