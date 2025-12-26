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

import java.util.Set;

/**
 * Generated Plain Old Java Objects class for {@link DiscoverResponse} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class DiscoverResponse extends PluginMessage {

    private class Device {
    }

    private class DiscoverResponseBody {
        public final Set<Device> devices = Set.of();
        public final boolean success = true;
    }

    private final DiscoverResponseBody body;

    public DiscoverResponse(String id) {
        super(id, DISCOVER_RESPONSE_TYPE);
        this.body = new DiscoverResponseBody();
    }
}
