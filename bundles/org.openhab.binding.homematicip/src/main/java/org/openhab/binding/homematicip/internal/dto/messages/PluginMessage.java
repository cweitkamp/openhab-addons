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
package org.openhab.binding.homematicip.internal.dto.messages;

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.PLUGIN_ID;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Generated Plain Old Java Objects class for {@link PluginMessage} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PluginMessage {

    public static final String CONFIG_TEMPLATE_REQUEST_TYPE = "CONFIG_TEMPLATE_REQUEST";
    public static final String CONFIG_UPDATE_REQUEST_TYPE = "CONFIG_UPDATE_REQUEST";
    public static final String CREATE_USER_MESSAGE_REQUEST_TYPE = "CREATE_USER_MESSAGE_REQUEST";
    public static final String DISCOVER_REQUEST_TYPE = "DISCOVER_REQUEST";
    public static final String HMIP_SYSTEM_REQUEST_TYPE = "HMIP_SYSTEM_REQUEST";
    public static final String PLUGIN_STATE_REQUEST_TYPE = "PLUGIN_STATE_REQUEST";

    public static final String CONFIG_TEMPLATE_RESPONSE_TYPE = "CONFIG_TEMPLATE_RESPONSE";
    public static final String CONFIG_UPDATE_RESPONSE_TYPE = "CONFIG_UPDATE_RESPONSE";
    public static final String CREATE_USER_MESSAGE_RESPONSE_TYPE = "CREATE_USER_MESSAGE_RESPONSE";
    public static final String DISCOVER_RESPONSE_TYPE = "DISCOVER_RESPONSE";
    public static final String HMIP_SYSTEM_RESPONSE_TYPE = "HMIP_SYSTEM_RESPONSE";
    public static final String HMIP_SYSTEM_EVENT_TYPE = "HMIP_SYSTEM_EVENT";
    public static final String PLUGIN_STATE_RESPONSE_TYPE = "PLUGIN_STATE_RESPONSE";

    public final String id;
    public final String pluginId = PLUGIN_ID;
    public final String type;

    public PluginMessage(String type) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
    }

    public PluginMessage(String id, String type) {
        this.id = id;
        this.type = type;
    }
}
