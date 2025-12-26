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

/**
 * Generated Plain Old Java Objects class for {@link ConfigUpdateResponse} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class ConfigUpdateResponse extends PluginMessage {

    public static final String CONFIG_UPDATE_RESPONSE_STATUS_APPLIED = "APPLIED";
    public static final String CONFIG_UPDATE_RESPONSE_STATUS_FAILED = "FAILED";
    public static final String CONFIG_UPDATE_RESPONSE_STATUS_PENDING = "PENDING";

    private class ConfigUpdateResponseBody {
        public final String status = CONFIG_UPDATE_RESPONSE_STATUS_APPLIED;
    }

    private final ConfigUpdateResponseBody body;

    public ConfigUpdateResponse(String id) {
        super(id, CONFIG_UPDATE_RESPONSE_TYPE);
        this.body = new ConfigUpdateResponseBody();
    }
}
