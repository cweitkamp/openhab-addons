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

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;

/**
 * Generated Plain Old Java Objects class for {@link HmipSystemResponse} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class HmipSystemResponse extends PluginMessage {

    public class HmipSystemResponseBody {
        public int code;
        public @Nullable JsonObject body;
    }

    public final HmipSystemResponseBody body;

    public HmipSystemResponse() {
        super(HMIP_SYSTEM_RESPONSE_TYPE);
        this.body = new HmipSystemResponseBody();
    }
}
