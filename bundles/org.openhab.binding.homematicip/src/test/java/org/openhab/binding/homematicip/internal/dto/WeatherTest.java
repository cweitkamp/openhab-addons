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
 * Test cases for {@link Weather} Plain Old Java Objects.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class WeatherTest extends AbstractTest {

    @Test
    public void weatherUpdateTest() throws IOException {
        Weather weatherMessage = getObjectFromJson("weather.json", Weather.class, gson);
        assertNotNull(weatherMessage);

        assertEquals(28.8, weatherMessage.temperature);
        assertEquals("CLEAR", weatherMessage.weatherCondition);
        assertEquals(27.9, weatherMessage.minTemperature);
        assertEquals(29.6, weatherMessage.maxTemperature);
        assertEquals(35, weatherMessage.humidity);
        assertEquals(6.444, weatherMessage.windSpeed);
        assertEquals(87, weatherMessage.windDirection);
    }
}
