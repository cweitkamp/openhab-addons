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
package org.openhab.binding.homematicip.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Test cases for {@link CurrentState} Plain Old Java Objects.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class CurrentStateTest extends AbstractTest {

    @Test
    public void currentStateUpdateTest() throws IOException {
        CurrentState currentStateMessage = getObjectFromJson("current-state.json", CurrentState.class, gson);
        assertNotNull(currentStateMessage);
        assertNotNull(currentStateMessage.devices);

        Map<String, JsonObject> devices = currentStateMessage.devices.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsJsonObject()));
        assertEquals(2, devices.size());

        JsonObject device = devices.get("3014F711A0000A9D89B64B9A");
        assertNotNull(device);
        assertEquals("3014F711A0000A9D89B64B9A", device.get("id").getAsString());

        device = devices.get("3014F711A0000A9D89B64D2C");
        assertNotNull(device);
        assertEquals("3014F711A0000A9D89B64D2C", device.get("id").getAsString());
    }
}
