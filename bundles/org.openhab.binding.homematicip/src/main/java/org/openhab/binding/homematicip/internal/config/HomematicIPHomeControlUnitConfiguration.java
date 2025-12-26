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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HomematicIPHomeControlUnitConfiguration} class contains fields mapping Thing configuration parameters.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPHomeControlUnitConfiguration {
    public String SGTIN = "";
    public String activationKey = "";

    public @Nullable String authtoken;

    /**
     * Gets the last four digits of the SGTIN.
     *
     * @return last four digits of the SGTIN
     * @throws IndexOutOfBoundsException
     */
    String lastFourDigitsOfSGTIN() throws IndexOutOfBoundsException {
        if (SGTIN.length() == 4) {
            return SGTIN;
        }
        if (SGTIN.length() > 4) {
            return SGTIN.substring(SGTIN.length() - 4);
        } else {
            throw new IndexOutOfBoundsException("SGTIN has fewer than 4 characters!");
        }
    }

    public String getURL() {
        return "https://hcu1-" + lastFourDigitsOfSGTIN() + ".local:6969";
    }

    public String getWebsocketURL() {
        return "wss://hcu1-" + lastFourDigitsOfSGTIN() + ".local:9001";
    }
}
