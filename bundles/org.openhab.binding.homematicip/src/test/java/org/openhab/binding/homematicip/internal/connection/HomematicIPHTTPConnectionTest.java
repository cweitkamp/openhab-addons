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
package org.openhab.binding.homematicip.internal.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homematicip.internal.config.HomematicIPAccessPointConfiguration;
import org.openhab.binding.homematicip.internal.dto.Host;
import org.openhab.binding.homematicip.internal.dto.params.GetCurrentStateParams;

import com.google.gson.Gson;

/**
 * Test cases for {@link HomematicIPHTTPConnectionTest}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPHTTPConnectionTest {

    private static final HomematicIPAccessPointConfiguration CONFIG = new HomematicIPAccessPointConfiguration();
    private static final String ID = "12345ABCDE";
    private static final String NEW_BASE_URL = "https://www.example.org/";

    private final Gson gson = new Gson();

    private @NonNullByDefault({}) HomematicIPHTTPConnection httpConnection;

    @BeforeEach
    public void setUp() {
        httpConnection = new HomematicIPHTTPConnection(mock(HttpClient.class), gson, CONFIG);
    }

    @Test
    void testForValidGetCurrentStateParams() {
        GetCurrentStateParams getCurrentStateParams = new GetCurrentStateParams();
        getCurrentStateParams.id = ID;

        assertEquals(
                "{\"clientCharacteristics\":{\"apiVersion\":\"10\",\"applicationIdentifier\":\"homematicip-python\",\"applicationVersion\":\"1.0\",\"deviceManufacturer\":\"none\",\"deviceType\":\"Computer\",\"language\":\"de_DE\",\"osType\":\"Linux\",\"osVersion\":\"5.15.32-v7+\"},\"id\":\""
                        + ID + "\"}",
                gson.toJson(getCurrentStateParams));
    }

    @Test
    void testBaseUrlChanges() {
        assertEquals(Host.REST_BASE_URL + HomematicIPHTTPConnection.GET_CURRENT_STATE_PATH,
                httpConnection.getCurrentStateUrl.toString());
        assertEquals(Host.REST_BASE_URL + HomematicIPHTTPConnection.SET_SET_ZONES_ACTIVATION_PATH,
                httpConnection.setSetZonesActivationUrl.toString());
        assertEquals(Host.REST_BASE_URL + HomematicIPHTTPConnection.SET_SET_POINT_TEMPERATURE_PATH,
                httpConnection.setSetPointTemperatureUrl.toString());
        assertEquals(Host.REST_BASE_URL + HomematicIPHTTPConnection.SET_CONTROL_MODE_PATH,
                httpConnection.setControlModeUrl.toString());
        assertEquals(Host.REST_BASE_URL + HomematicIPHTTPConnection.SET_ACTIVE_PROFILE_PATH,
                httpConnection.setActiveProfileUrl.toString());
        assertEquals(Host.REST_BASE_URL + HomematicIPHTTPConnection.SET_CLIMATE_CONTROL_DISPLAY_PATH,
                httpConnection.setClimateControlDisplayUrl.toString());
        assertEquals(Host.REST_BASE_URL + HomematicIPHTTPConnection.SET_SWITCH_STATE_PATH,
                httpConnection.setSwitchStateUrl.toString());

        httpConnection.setBaseUrl(NEW_BASE_URL);
        assertEquals(NEW_BASE_URL + HomematicIPHTTPConnection.GET_CURRENT_STATE_PATH,
                httpConnection.getCurrentStateUrl.toString());
        assertEquals(NEW_BASE_URL + HomematicIPHTTPConnection.SET_SET_ZONES_ACTIVATION_PATH,
                httpConnection.setSetZonesActivationUrl.toString());
        assertEquals(NEW_BASE_URL + HomematicIPHTTPConnection.SET_SET_POINT_TEMPERATURE_PATH,
                httpConnection.setSetPointTemperatureUrl.toString());
        assertEquals(NEW_BASE_URL + HomematicIPHTTPConnection.SET_CONTROL_MODE_PATH,
                httpConnection.setControlModeUrl.toString());
        assertEquals(NEW_BASE_URL + HomematicIPHTTPConnection.SET_ACTIVE_PROFILE_PATH,
                httpConnection.setActiveProfileUrl.toString());
        assertEquals(NEW_BASE_URL + HomematicIPHTTPConnection.SET_CLIMATE_CONTROL_DISPLAY_PATH,
                httpConnection.setClimateControlDisplayUrl.toString());
        assertEquals(NEW_BASE_URL + HomematicIPHTTPConnection.SET_SWITCH_STATE_PATH,
                httpConnection.setSwitchStateUrl.toString());
    }
}
