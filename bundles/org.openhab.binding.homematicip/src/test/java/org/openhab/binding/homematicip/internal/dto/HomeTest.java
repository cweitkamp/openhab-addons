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
 * Test cases for {@link Home} Plain Old Java Objects.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomeTest extends AbstractTest {

    @Test
    public void homeUpdateTest() throws IOException {
        Home homeMessage = getObjectFromJson("home.json", Home.class, gson);
        assertNotNull(homeMessage);
        assertNotNull(homeMessage.weather);
        assertInstanceOf(Weather.class, homeMessage.weather);
    }
}
