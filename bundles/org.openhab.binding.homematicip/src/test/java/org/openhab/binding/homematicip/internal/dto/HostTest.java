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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link Host} Plain Old Java Objects.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HostTest extends AbstractTest {

    @Test
    public void hostUpdateTest() throws IOException {
        Host hostMessage = getObjectFromJson("host.json", Host.class, gson);
        assertNotNull(hostMessage);

        assertEquals("https://srz14.homematic.com:6969", hostMessage.urlREST);
        assertEquals("wss://srz14.homematic.com:8888", hostMessage.urlWebSocket);
        assertEquals("12", hostMessage.apiVersion);
        assertEquals("AAJIG8NMG34KD99Z8FP6V01W", hostMessage.primaryAccessPointId);
        assertEquals("AAJIG8NMG34KD99Z8FP6V01W", hostMessage.requestingAccessPointId);
    }
}
