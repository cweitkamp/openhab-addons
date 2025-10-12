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

import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link HomematicIPAccessPointConfiguration}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPAccessPointConfigurationTest {

    private static final HomematicIPAccessPointConfiguration CONFIG = new HomematicIPAccessPointConfiguration();

    @BeforeEach
    public void setUp() {
        CONFIG.SGTIN = "AAJIG8NMG34KD99Z8FP6V01W";
    }

    @Test
    void testGenerateClientauthIsValid() throws NoSuchAlgorithmException {
        assertNull(CONFIG.clientauth);

        CONFIG.generateClientauth();

        assertEquals(
                "80BD10A8ECF8C4F5F173A2F82B364A511A46AD4B26EAEA3B5153C4DF6D5E0B9A91F1D1DB29C7479EC8938D629C12F4246859ECFA93525A3552F7CC8E8943D903",
                CONFIG.clientauth);
    }
}
