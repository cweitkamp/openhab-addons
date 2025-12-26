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

import java.util.Map;

import com.google.gson.JsonObject;

/**
 * Generated Plain Old Java Objects class for {@link ConfigTemplateResponse} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class ConfigTemplateResponse extends PluginMessage {

    private class ConfigTemplateResponseBody {
        public final Map<String, JsonObject> groups = Map.of();
        public final Map<String, JsonObject> properties = Map.of();
    }

    private final ConfigTemplateResponseBody body;

    public ConfigTemplateResponse(String id) {
        super(id, CONFIG_TEMPLATE_RESPONSE_TYPE);
        this.body = new ConfigTemplateResponseBody();
    }
}
