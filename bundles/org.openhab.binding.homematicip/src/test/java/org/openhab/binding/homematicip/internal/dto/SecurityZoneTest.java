/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Test cases for {@link SecurityZone} Plain Old Java Objects.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
class SecurityZoneTest extends AbstractTest {

    @Test
    public void securityZoneUpdateTest() throws IOException {
        SecurityZone securityZoneMessage = getObjectFromJson("security-zone.json", SecurityZone.class, gson);
        assertNotNull(securityZoneMessage);

        assertEquals("63611d82-43b5-4f3a-b5f3-02700f263878", securityZoneMessage.id);
        assertEquals("0aa05b9f-6c4b-4dcf-85d6-e489754b4edd", securityZoneMessage.homeId);
        assertEquals("EXTERNAL", securityZoneMessage.label);
        assertEquals(1679329177946L, securityZoneMessage.lastStatusUpdate);
        assertEquals("SECURITY_ZONE", securityZoneMessage.type);
        assertTrue(securityZoneMessage.active);
        assertFalse(securityZoneMessage.silent);
        assertEquals("CLOSED", securityZoneMessage.windowState);
        assertEquals(Boolean.FALSE, securityZoneMessage.configPending);
    }
}
