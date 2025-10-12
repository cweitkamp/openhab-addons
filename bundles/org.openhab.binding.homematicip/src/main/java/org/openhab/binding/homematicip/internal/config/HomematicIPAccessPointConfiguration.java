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
package org.openhab.binding.homematicip.internal.config;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HomematicIPAccessPointConfiguration} class contains fields mapping Thing configuration parameters.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPAccessPointConfiguration {
    public String SGTIN = "";
    public String authcode = "";
    public int refreshInterval = 10;

    public @Nullable String clientauth;

    /**
     * Generates the client authentication token based on the SGTIN.
     *
     * @throws NoSuchAlgorithmException
     */
    public void generateClientauth() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] bytes = (SGTIN + "jiLpVitHvWnIGD1yo7MA").getBytes(StandardCharsets.UTF_8);
        clientauth = String.format("%032x", new BigInteger(1, md.digest(bytes))).toUpperCase();
    }
}
