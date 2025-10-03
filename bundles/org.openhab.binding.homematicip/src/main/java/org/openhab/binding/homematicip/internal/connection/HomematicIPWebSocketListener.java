/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * Informs {@link HomematicIPWebSocketConnection} listeners about events and messages.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public interface HomematicIPWebSocketListener {

    /**
     * Called on websocket connected.
     */
    void onConnect();

    /**
     * Called on websocket closed.
     *
     * @param reason The reason why the websocket connection was closed (e.g. "Disconnected" or "Broken pipe")
     */
    void onClose(String reason);

    /**
     * Called on websocket errors or exceptions.
     */
    void onError();

    /**
     * Updates all channels of this listener from the latest data retrieved.
     *
     * @param data The Json data
     */
    void messageReceived(JsonObject data);
}
