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
package org.openhab.binding.gruenstromindex.internal.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.openhab.binding.gruenstromindex.internal.GruenstromIndexBindingConstants.*;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GruenstromIndexZipcodeConfigOptionProvider} methods.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GruenstromIndexZipcodeConfigOptionProviderTest {

    private static final int NUMBER_OF_ZIPCODES = 16477;
    @NonNullByDefault({})
    GruenstromIndexZipcodeConfigOptionProvider provider;

    @BeforeEach
    public void setup() {
        provider = new GruenstromIndexZipcodeConfigOptionProvider();
        provider.activate();
    }

    @Test
    void testZipcodesAreImportedAndConvertedFromJSON() {
        assertThat(provider.getZipcodes(), hasSize(NUMBER_OF_ZIPCODES));
    }

    @Test
    void testProviderReturnsNoParameterOptionsIfURIIsWrong() {
        assertNull(
                provider.getParameterOptions(URI.create(THING_TYPE_ACCOUNT.getAsString()), CONFIG_ZIPCODE, null, null));
    }

    @Test
    void testProviderReturnsNoParameterOptionsIfParamIsWrong() {
        assertNull(provider.getParameterOptions(URI.create(THING_TYPE_GREEN_ENERGY_FORECAST.getAsString()), "foobar",
                null, null));
    }

    @Test
    void testProviderReturnsParameterOptions() {
        assertThat(provider.getParameterOptions(URI.create(THING_TYPE_GREEN_ENERGY_FORECAST.getAsString()),
                CONFIG_ZIPCODE, null, null), hasSize(NUMBER_OF_ZIPCODES));
    }
}
