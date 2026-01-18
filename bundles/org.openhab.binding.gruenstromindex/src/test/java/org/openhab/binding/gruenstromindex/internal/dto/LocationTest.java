/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.gruenstromindex.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.ParameterOption;

/**
 * Tests for {@link Location} methods.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class LocationTest {

    public static final String ZIPCODE = "12345";
    public static final String NAME = "foobar";

    @Test
    void testLocationReturnValidParameterOption() {
        Location location = new Location(ZIPCODE, NAME);

        ParameterOption options = location.getAsParameterOption();
        assertEquals(ZIPCODE, options.getValue());
        assertEquals(NAME, options.getLabel());
    }
}
