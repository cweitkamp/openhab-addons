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

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ParameterOption;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Location} is the Java class used to map the JSON response to a GruenstromIndex API request.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class Location {

    public static final Comparator<Location> COMPARATOR = new Comparator<>() {

        @Override
        public int compare(Location a, Location b) {
            return a.zipcode.compareTo(b.zipcode);
        }
    };

    @SerializedName(value = "zipcode", alternate = { "zip" })
    public String zipcode;
    @SerializedName(value = "place", alternate = { "city" })
    public String place;
    @SerializedName("signature")
    public @Nullable String signature;

    public Location(String zipcode, String place) {
        this.zipcode = zipcode;
        this.place = place;
    }

    /**
     *
     *
     * @return
     */
    public ParameterOption getAsParameterOption() {
        return new ParameterOption(zipcode, place);
    }
}
