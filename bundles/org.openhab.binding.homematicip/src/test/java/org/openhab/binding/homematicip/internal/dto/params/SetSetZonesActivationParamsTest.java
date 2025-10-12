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
package org.openhab.binding.homematicip.internal.dto.params;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homematicip.internal.dto.AbstractTest;

/**
 * Test cases for {@link SetSetZonesActivationParams} Plain Old Java Objects.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class SetSetZonesActivationParamsTest extends AbstractTest {

    @Test
    public void currentStateUpdateTest() throws IOException {
        SetSetZonesActivationParams setSetZonesActivationParams = new SetSetZonesActivationParams();
        assertNotNull(setSetZonesActivationParams.zonesActivation);
        assertFalse(setSetZonesActivationParams.zonesActivation.external);
        assertFalse(setSetZonesActivationParams.zonesActivation.internal);

        setSetZonesActivationParams.zonesActivation.external = true;

        String json = gson.toJson(setSetZonesActivationParams);
        assertEquals("{\"zonesActivation\":{\"EXTERNAL\":true,\"INTERNAL\":false}}", json);
    }
}
