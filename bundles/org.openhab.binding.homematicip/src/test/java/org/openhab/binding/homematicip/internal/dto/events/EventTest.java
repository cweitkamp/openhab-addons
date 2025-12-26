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
package org.openhab.binding.homematicip.internal.dto.events;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homematicip.internal.dto.AbstractGsonTest;

import com.google.gson.JsonObject;

/**
 * Test cases for {@link Event} Plain Old Java Objects.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class EventTest extends AbstractGsonTest {

    @Test
    public void homeChangedEventTest() throws IOException {
        Event event = getFirstEventFromJson("home_changed_event.json");
        assertNotNull(event);

        assertEquals(Event.EVENT_TYPE_HOME_CHANGED, event.eventType);
        assertNull(event.device);
        assertNull(event.group);
        assertNotNull(event.home);
    }

    @Test
    public void deviceChangedEventTest() throws IOException {
        Event event = getFirstEventFromJson("device_changed_event.json");
        assertNotNull(event);

        assertEquals(Event.EVENT_TYPE_DEVICE_CHANGED, event.eventType);
        assertNotNull(event.device);
        assertNull(event.group);
        assertNull(event.home);
    }

    private @Nullable Event getFirstEventFromJson(String filename) throws IOException {
        WebSocketEvent webSocketEvent = getObjectFromJson(filename, WebSocketEvent.class, gson);
        assertNotNull(webSocketEvent);

        assertEquals(Origin.ORIGIN_TYPE_DEVICE, webSocketEvent.origin.originType);

        Map<String, JsonObject> events = webSocketEvent.events.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsJsonObject()));

        return gson.fromJson(events.get("0"), Event.class);
    }
}
