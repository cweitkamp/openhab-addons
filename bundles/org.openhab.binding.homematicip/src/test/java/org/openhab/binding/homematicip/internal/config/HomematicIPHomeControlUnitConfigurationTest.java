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
package org.openhab.binding.homematicip.internal.config;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The {@link HomematicIPHomeControlUnitConfiguration} class contains fields mapping Thing configuration parameters.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPHomeControlUnitConfigurationTest {

    private static final HomematicIPHomeControlUnitConfiguration CONFIG = new HomematicIPHomeControlUnitConfiguration();
    private static final String SGTIN_POSTFIX = "V01W";

    @BeforeEach
    public void setUp() {
        CONFIG.SGTIN = "AAJIG8NMG34KD99Z8FP6" + SGTIN_POSTFIX;
    }

    @Test
    void testLastFourDigitsOfSGTINThrowsException() {
        CONFIG.SGTIN = "";

        assertThrows(IndexOutOfBoundsException.class, () -> CONFIG.lastFourDigitsOfSGTIN());
    }

    @Test
    void testLastFourDigitsOfSGTINIfLengthOfSGTINEqualsToFour() throws IndexOutOfBoundsException {
        CONFIG.SGTIN = SGTIN_POSTFIX;

        assertEquals(SGTIN_POSTFIX, CONFIG.lastFourDigitsOfSGTIN());
    }

    @Test
    void testLastFourDigitsOfSGTINIfLengthOfSGTINIsGreaterThanFour() throws IndexOutOfBoundsException {
        assertEquals(SGTIN_POSTFIX, CONFIG.lastFourDigitsOfSGTIN());
    }

    @Test
    void testgetURL() throws IndexOutOfBoundsException {
        assertEquals("https://hcu1-" + SGTIN_POSTFIX + ".local:6969", CONFIG.getURL());
    }

    @Test
    void testgetWebsocketURL() throws IndexOutOfBoundsException {
        assertEquals("wss://hcu1-" + SGTIN_POSTFIX + ".local:9001", CONFIG.getWebsocketURL());
    }
}
