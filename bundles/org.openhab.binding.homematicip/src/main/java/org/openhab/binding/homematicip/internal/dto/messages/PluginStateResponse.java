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

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.PLUGIN_TITLE;

/**
 * Generated Plain Old Java Objects class for {@link PluginStateResponse} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class PluginStateResponse extends PluginMessage {

    public static final String PLUGIN_READINESS_STATUS_CONFIG_REQUIRED = "CONFIG_REQUIRED";
    public static final String PLUGIN_READINESS_STATUS_ERROR = "ERROR";
    public static final String PLUGIN_READINESS_STATUS_READY = "READY";

    private class FriendlyName {
        public final String en = PLUGIN_TITLE;
        public final String de = PLUGIN_TITLE;
    }

    private class PluginStateResponseBody {
        public final String pluginReadinessStatus;
        public final FriendlyName friendlyName = new FriendlyName();

        public PluginStateResponseBody(String pluginStatus) {
            this.pluginReadinessStatus = pluginStatus;
        }
    }

    private final PluginStateResponseBody body;

    public PluginStateResponse() {
        super(PLUGIN_STATE_RESPONSE_TYPE);
        this.body = new PluginStateResponseBody(PLUGIN_READINESS_STATUS_READY);
    }

    public PluginStateResponse(String id) {
        super(id, PLUGIN_STATE_RESPONSE_TYPE);
        this.body = new PluginStateResponseBody(PLUGIN_READINESS_STATUS_READY);
    }
}
