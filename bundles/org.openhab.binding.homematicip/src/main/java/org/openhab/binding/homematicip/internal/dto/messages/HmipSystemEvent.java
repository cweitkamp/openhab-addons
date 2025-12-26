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

import com.google.gson.JsonObject;

/**
 * Generated Plain Old Java Objects class for {@link HmipSystemEvent} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class HmipSystemEvent extends PluginMessage {

    public class HmipSystemEventBody {
        public JsonObject eventTransaction;
    }

    public final HmipSystemEventBody body;

    public HmipSystemEvent() {
        super(HMIP_SYSTEM_EVENT_TYPE);
        this.body = new HmipSystemEventBody();
    }
}
